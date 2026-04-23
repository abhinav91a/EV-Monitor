import { Component }     from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule }   from '@angular/forms';
import { StationService } from '../../services/station.service';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss']
})
export class SearchBarComponent {

  selectedBorough = '';
  selectedStatus  = '';

  boroughs = [
    'Westminster', 'City of London', 'Tower Hamlets',
    'Hackney', 'Islington', 'Camden', 'Southwark',
    'Lambeth', 'Wandsworth', 'Hammersmith and Fulham',
    'Kensington and Chelsea', 'Greenwich', 'Lewisham'
  ];

  statuses = ['AVAILABLE', 'OCCUPIED', 'FAULTY', 'UNKNOWN'];

  constructor(private stationService: StationService) {}

  onBoroughChange(): void {
    if (this.selectedBorough) {
      this.stationService.loadByBorough(this.selectedBorough);
    } else {
      this.stationService.loadAllStations();
    }
  }

  onStatusChange(): void {
    if (this.selectedStatus) {
      this.stationService.loadByStatus(this.selectedStatus);
    } else {
      this.stationService.loadAllStations();
    }
  }
}
