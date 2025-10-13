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

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador MVC que expone la versión web del juego.
 */
@Controller
@RequestMapping
public class GameController {

    private final ConcurrentGame game;

    public GameController(ConcurrentGame game) {
        this.game = game;
    }

    @GetMapping("/")
    public String landing(Model model, HttpSession session) {
        if (session.getAttribute("playerId") != null) {
            return "redirect:/game";
        }

        if (!model.containsAttribute("playerForm")) {
            model.addAttribute("playerForm", new PlayerForm());
        }
        model.addAttribute("summary", game.snapshot());
        return "landing";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute("playerForm") PlayerForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("summary", game.snapshot());
            return "landing";
        }

        try {
            PlayerView view = game.registerPlayer(form.getAlias());
            session.setAttribute("playerId", view.id());
            redirectAttributes.addFlashAttribute("welcomeMessage",
                    "Bienvenido, " + view.alias() + ". Ya puedes jugar.");
            return "redirect:/game";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("alias", "alias.invalid", ex.getMessage());
            model.addAttribute("summary", game.snapshot());
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
        redirectAttributes.addFlashAttribute("infoMessage", "Se ha reiniciado la partida.");
        return "redirect:/game";
    }

    @PostMapping("/abandon")
    public String abandon(HttpSession session) {
        session.removeAttribute("playerId");
        return "redirect:/";
    }
}
