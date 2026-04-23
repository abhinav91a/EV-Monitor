import { Component }     from '@angular/core';
import { CommonModule }  from '@angular/common';
import { StationService } from '../../services/station.service';

@Component({
  selector: 'app-station-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './station-detail.component.html',
  styleUrls: ['./station-detail.component.scss']
})
export class StationDetailComponent {
  constructor(public stationService: StationService) {}

  close(): void {
    this.stationService.selected.set(null);
  }

  getStatusClass(status: string): string {
    return status?.toLowerCase() ?? 'unknown';
  }
}
