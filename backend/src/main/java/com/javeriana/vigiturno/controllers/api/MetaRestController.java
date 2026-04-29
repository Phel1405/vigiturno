package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.MetaDto;
import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.models.enums.SeveridadIncidente;
import com.javeriana.vigiturno.models.enums.TipoIncidente;
import com.javeriana.vigiturno.models.enums.TipoNotificacion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
public class MetaRestController {

    @GetMapping
    public MetaDto meta() {
        return new MetaDto(
                RolNombre.values(),
                EstadoTurno.values(),
                TipoIncidente.values(),
                SeveridadIncidente.values(),
                TipoNotificacion.values(),
                EstadoReasignacion.values()
        );
    }
}
