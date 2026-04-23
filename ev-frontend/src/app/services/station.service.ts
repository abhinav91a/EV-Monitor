import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChargingStation } from '../models/charging-station';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StationService {

  // Signals for reactive state (Angular 17 way)
  stations   = signal<ChargingStation[]>([]);
  selected   = signal<ChargingStation | null>(null);
  loading    = signal<boolean>(false);
  error      = signal<string | null>(null);

  constructor(private http: HttpClient) {}

  // Load all stations from your Spring Boot backend
  loadAllStations(): void {
    this.loading.set(true);
    this.error.set(null);

    this.http.get<ChargingStation[]>(`${environment.apiBaseUrl}/stations`)
      .subscribe({
        next: (data) => {
          this.stations.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set('Failed to load stations. Is your backend running?');
          this.loading.set(false);
          console.error(err);
        }
      });
  }

  // Filter by borough
  loadByBorough(borough: string): void {
    this.loading.set(true);
    this.http.get<ChargingStation[]>(
      `${environment.apiBaseUrl}/stations/borough/${borough}`
    ).subscribe({
      next: (data) => { this.stations.set(data); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }

  // Filter by status
  loadByStatus(status: string): void {
    this.loading.set(true);
    this.http.get<ChargingStation[]>(
      `${environment.apiBaseUrl}/stations/status/${status}`
    ).subscribe({
      next: (data) => { this.stations.set(data); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }

  // Select a station (shows detail panel)
  selectStation(station: ChargingStation): void {
    this.selected.set(station);
  }

  // Update a single station's status in the signal
  // (called by WebSocket when live update arrives)
  updateStationStatus(uuid: string, newStatus: string, available: number): void {
    this.stations.update(list =>
      list.map(s =>
        s.ocmUuid === uuid
          ? { ...s, currentStatus: newStatus as any, availableConnectors: available }
          : s
      )
    );
  }
}
