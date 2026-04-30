package com.javeriana.vigiturno.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("titulo", "Recurso no encontrado");
        model.addAttribute("codigo", "404");
        model.addAttribute("mensaje", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        model.addAttribute("titulo", "Operación no permitida");
        model.addAttribute("codigo", "400");
        model.addAttribute("mensaje", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, Model model) {
        model.addAttribute("titulo", "Error de integridad de datos");
        model.addAttribute("codigo", "400");
        model.addAttribute("mensaje", "No se pudo completar la operación porque los datos violan una restricción de la base de datos.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("titulo", "Error interno");
        model.addAttribute("codigo", "500");
        model.addAttribute("mensaje", "Ocurrió un error inesperado en el sistema.");
        return "error/error";
    }
}