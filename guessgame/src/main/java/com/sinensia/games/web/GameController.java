package com.sinensia.games.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sinensia.games.ConcurrentGame;
import com.sinensia.games.ConcurrentGame.GameSummary;
import com.sinensia.games.ConcurrentGame.GuessResult;
import com.sinensia.games.ConcurrentGame.PlayerView;
import com.sinensia.games.model.Partida;
import com.sinensia.games.model.Partida.Resultado;
import com.sinensia.games.service.DuplicateAliasException;
import com.sinensia.games.service.InvalidCredentialsException;
import com.sinensia.games.service.PartidaService;
import com.sinensia.games.service.PlayerRegistrationService;
import com.sinensia.games.service.PlayerRegistrationService.RegistrationResult;
import com.sinensia.games.service.RankingService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador MVC que expone la versión web del juego.
 */
@Controller
@RequestMapping
public class GameController {

    private final ConcurrentGame game;
    private final PlayerRegistrationService registrationService;
    private final PartidaService partidaService;
    private final RankingService rankingService;

    public GameController(ConcurrentGame game, PlayerRegistrationService registrationService,
            PartidaService partidaService, RankingService rankingService) {
        this.game = game;
        this.registrationService = registrationService;
        this.partidaService = partidaService;
        this.rankingService = rankingService;
    }

    @GetMapping("/")
    public String landing(Model model, HttpSession session) {
        if (session.getAttribute("playerId") != null) {
            return "redirect:/game";
        }

        if (!model.containsAttribute("playerForm")) {
            model.addAttribute("playerForm", new PlayerForm());
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        model.addAttribute("summary", game.snapshot());
        model.addAttribute("ranking", rankingService.obtenerRanking());
        return "landing";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute("playerForm") PlayerForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("playerId") != null) {
            return "redirect:/game";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("summary", game.snapshot());
            model.addAttribute("ranking", rankingService.obtenerRanking());
            return "landing";
        }

        try {
            RegistrationResult registration = registrationService.registrarJugador(form);
            PlayerView view = game.registerPlayer(registration.jugador().getAlias());
            session.setAttribute("playerId", view.id());
            session.setAttribute("jugadorId", registration.jugador().getId());
            session.setAttribute("partidaId", registration.partida().getId());

            redirectAttributes.addFlashAttribute("welcomeMessage",
                    "Bienvenido, " + view.alias() + ". Ya puedes jugar.");
            redirectAttributes.addFlashAttribute("alias", registration.jugador().getAlias());
            redirectAttributes.addFlashAttribute("edad", registration.jugador().getEdad());
            redirectAttributes.addFlashAttribute("partidaId", registration.partida().getId());
            return "redirect:/registro/exito";
        } catch (DuplicateAliasException ex) {
            bindingResult.rejectValue("alias", "alias.duplicated", ex.getMessage());
            model.addAttribute("summary", game.snapshot());
            if (!model.containsAttribute("loginForm")) {
                model.addAttribute("loginForm", new LoginForm());
            }
            model.addAttribute("ranking", rankingService.obtenerRanking());
            return "landing";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("alias", "alias.invalid", ex.getMessage());
            model.addAttribute("summary", game.snapshot());
            if (!model.containsAttribute("loginForm")) {
                model.addAttribute("loginForm", new LoginForm());
            }
            model.addAttribute("ranking", rankingService.obtenerRanking());
            return "landing";
        }
    }

    @GetMapping("/game")
    public String panel(Model model, HttpSession session) {
        String playerId = (String) session.getAttribute("playerId");
        if (playerId == null) {
            return "redirect:/";
        }

        PlayerView player = game.getPlayer(playerId);
        GameSummary summary = game.snapshot();

        if (!model.containsAttribute("guessForm")) {
            model.addAttribute("guessForm", new GuessForm());
        }

        model.addAttribute("player", player);
        model.addAttribute("summary", summary);
        model.addAttribute("ranking", rankingService.obtenerRanking());

        return "game";
    }

    @PostMapping("/guess")
    public String guess(@Valid @ModelAttribute("guessForm") GuessForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String playerId = (String) session.getAttribute("playerId");
        if (playerId == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            return panel(model, session);
        }

        try {
            GuessResult result = game.submitGuess(playerId, form.getGuess());
            redirectAttributes.addFlashAttribute("lastResult", result);
            actualizarEstadoPartida(session, result);
            return "redirect:/game";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/reset")
    public String reset(HttpSession session, RedirectAttributes redirectAttributes) {
        String playerId = (String) session.getAttribute("playerId");
        if (playerId == null) {
            return "redirect:/";
        }

        GameSummary summary = game.snapshot();
        if (!summary.finished()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "La partida sigue en curso. Solo se puede reiniciar cuando concluye.");
            return "redirect:/game";
        }

        game.resetGame();
        Long jugadorId = (Long) session.getAttribute("jugadorId");
        if (jugadorId != null) {
            Partida nuevaPartida = registrationService.iniciarNuevaPartida(jugadorId);
            session.setAttribute("partidaId", nuevaPartida.getId());
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Se ha reiniciado la partida. Nueva partida #" + nuevaPartida.getId());
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "Se ha reiniciado la partida.");
        }
        return "redirect:/game";
    }

    @PostMapping("/abandon")
    public String abandon(HttpSession session) {
        Long partidaId = (Long) session.getAttribute("partidaId");
        if (partidaId != null) {
            partidaService.abandonarPartida(partidaId);
        }
        session.removeAttribute("playerId");
        session.removeAttribute("partidaId");
        session.removeAttribute("jugadorId");
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("playerId") != null) {
            return "redirect:/game";
        }

        if (bindingResult.hasErrors()) {
            if (!model.containsAttribute("playerForm")) {
                model.addAttribute("playerForm", new PlayerForm());
            }
            model.addAttribute("summary", game.snapshot());
            model.addAttribute("ranking", rankingService.obtenerRanking());
            return "landing";
        }

        try {
            var jugador = registrationService.autenticarJugador(form);
            PlayerView view = game.registerPlayer(jugador.getAlias());
            Partida partida = registrationService.iniciarNuevaPartida(jugador.getId());

            session.setAttribute("playerId", view.id());
            session.setAttribute("jugadorId", jugador.getId());
            session.setAttribute("partidaId", partida.getId());

            redirectAttributes.addFlashAttribute("infoMessage",
                    "Bienvenido de nuevo, " + jugador.getAlias() + ". ¡Nueva partida lista!");
            return "redirect:/game";
        } catch (InvalidCredentialsException ex) {
            bindingResult.rejectValue("alias", "login.invalid", ex.getMessage());
            if (!model.containsAttribute("playerForm")) {
                model.addAttribute("playerForm", new PlayerForm());
            }
            model.addAttribute("summary", game.snapshot());
            model.addAttribute("ranking", rankingService.obtenerRanking());
            return "landing";
        }
    }

    @GetMapping("/registro/exito")
    public String registroExito(Model model, HttpSession session) {
        if (!model.containsAttribute("alias")) {
            return "redirect:/";
        }
        model.addAttribute("summary", game.snapshot());
        model.addAttribute("ranking", rankingService.obtenerRanking());
        return "registro-exito";
    }

    private void actualizarEstadoPartida(HttpSession session, GuessResult result) {
        Long partidaId = (Long) session.getAttribute("partidaId");
        if (partidaId == null) {
            return;
        }
        if (result.estado() == com.sinensia.games.GuessGame.Estado.SUCCESS) {
            partidaService.finalizarPartida(partidaId, Resultado.GANADA);
        } else if (result.player().eliminated()) {
            partidaService.finalizarPartida(partidaId, Resultado.PERDIDA);
        } else if (result.finished() && !result.player().winner()) {
            partidaService.finalizarPartida(partidaId, Resultado.PERDIDA);
        }
    }
}
