import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Zona } from '../../core/models';

@Component({
  selector: 'app-zonas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './zonas.component.html'
})
export class ZonasComponent implements OnInit {
  zonas: Zona[] = [];
  editando = false;
  error = '';
  form: Zona = this.base();

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.api.zonas().subscribe({ next: data => this.zonas = data, error: () => this.error = 'No se pudieron cargar zonas.' }); }
  nuevo(): void { this.editando = false; this.form = this.base(); }
  editar(zona: Zona): void { this.editando = true; this.form = { ...zona }; }

  guardar(): void {
    const request = this.form.id ? this.api.actualizarZona(this.form.id, this.form) : this.api.crearZona(this.form);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar la zona.' });
  }

  eliminar(id: number): void {
    this.api.eliminarZona(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar la zona.' });
  }

  private base(): Zona { return { nombre: '', descripcion: '', capacidadMaxima: 30, activa: true }; }
}
