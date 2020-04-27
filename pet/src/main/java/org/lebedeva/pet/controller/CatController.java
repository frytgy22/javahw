package org.lebedeva.pet.controller;

import lombok.extern.slf4j.Slf4j;
import org.lebedeva.pet.dto.cat.CatBreedDto;
import org.lebedeva.pet.dto.cat.CatDto;
import org.lebedeva.pet.service.CatBreedService;
import org.lebedeva.pet.service.CatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(CatController.BASE_URL)
public class CatController {

    public static final String VIEW_PATH = "cat";
    public static final String BASE_URL = "/cats";
    public static final String FORM_PATH = VIEW_PATH + "/form";
    public static final String INDEX_PATH = VIEW_PATH + "/index";
    public static final String REDIRECT_INDEX = "redirect:" + BASE_URL;

    private Pageable pageable;

    private final CatService catService;
    private final CatBreedService catBreedService;

    public CatController(CatService catService, CatBreedService catBreedService) {
        this.catService = catService;
        this.catBreedService = catBreedService;
    }

    @ModelAttribute("catBreeds")
    List<CatBreedDto> getGroups() {
        return catBreedService.findCatBreedsDto();
    }

    @ModelAttribute("message")
    public void setMessage() {
    }

    @GetMapping
    public String index(Model model, Integer page, Integer size) {
        pageable = PageRequest.of(page == null ? 0 : page, size == null ? 5 : size, Sort.by("breed"));
        Page<CatDto> catsDtoPage = catService.findAll(pageable);

        model.addAttribute("url", BASE_URL);
        model.addAttribute("page", catsDtoPage);
        model.addAttribute("cats", catsDtoPage.getContent());
        return INDEX_PATH;
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("catDto", new CatDto());
        return FORM_PATH;
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute CatDto catDto,
                         BindingResult bindingResult,
                         RedirectAttributes attributes) {
        if (!bindingResult.hasErrors()) {
            try {
                catService.save(catDto);
                attributes.addFlashAttribute("message", "Saved successfully!");
            } catch (Exception e) {
                attributes.addFlashAttribute("message", "Saving failed!");
                log.error(e.getLocalizedMessage(), e);
            }
            return REDIRECT_INDEX;
        }
        log.error(bindingResult.toString());
        return FORM_PATH;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, RedirectAttributes attributes) {
        try {
            catService.delete(id);
            attributes.addFlashAttribute("message", "Deleted successfully!");
        } catch (Exception e) {
            attributes.addFlashAttribute("message", "Deletion failed!");
            log.error(e.getLocalizedMessage(), e);
        }
        return REDIRECT_INDEX;
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        try {
            model.addAttribute("catDto", catService.findById(id).orElseThrow(Exception::new));
            return FORM_PATH;
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return REDIRECT_INDEX;
    }

    @PostMapping("/edit/{id}")
    public String edit(@Validated @ModelAttribute CatDto catDto,
                       BindingResult bindingResult,
                       RedirectAttributes attributes) {
        return create(catDto, bindingResult, attributes);
    }
}
