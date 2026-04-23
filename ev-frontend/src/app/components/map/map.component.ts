import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { GoogleMap,
         GoogleMapsModule,
         MapMarker }               from '@angular/google-maps';
import { StationService }          from '../../services/station.service';
import { WebsocketService }        from '../../services/websocket.service';
import { SearchBarComponent }      from '../search-bar/search-bar.component';
import { StationDetailComponent }  from '../station-detail/station-detail.component';
import { ChargingStation }         from '../../models/charging-station';
import { environment }             from '../../../environments/environment';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [
    CommonModule,
    GoogleMapsModule,
    MapMarker,
    SearchBarComponent,
    StationDetailComponent
  ],
  templateUrl: './map.component.html',
  styleUrls:   ['./map.component.scss']
})
export class MapComponent implements OnInit {

  center  = { lat: 51.5074, lng: -0.1278 };
  zoom    = 11;
  mapOptions: google.maps.MapOptions = {
    mapTypeControl:    false,
    streetViewControl: false,
    fullscreenControl: false,
  };

  statusColours: Record<string, string> = {
    AVAILABLE : '#1E7E34',
    OCCUPIED  : '#856404',
    FAULTY    : '#721C24',
    UNKNOWN   : '#6C757D',
  };

  constructor(
    public stationService: StationService,
    private wsService: WebsocketService
  ) {}

  ngOnInit(): void {
    this.stationService.loadAllStations();
    this.wsService.connect();
  }

  getMarkerOptions(station: ChargingStation): google.maps.MarkerOptions {
    return {
      icon: {
        path: google.maps.SymbolPath.CIRCLE,
        scale: 10,
        fillColor:    this.statusColours[station.currentStatus] ?? '#6C757D',
        fillOpacity:  1,
        strokeColor:  '#FFFFFF',
        strokeWeight: 2,
      },
      title: `${station.operatorName} — ${station.currentStatus}`,
    };
  }

  onMarkerClick(station: ChargingStation): void {
    this.stationService.selectStation(station);
  }

  getStationsByStatus(status: string): ChargingStation[] {
    return this.stationService.stations()
      .filter(s => s.currentStatus === status);
  }
}