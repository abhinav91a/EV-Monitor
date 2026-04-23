import { Injectable } from '@angular/core';
import { StationService } from './station.service';
import { StatusChangeEvent } from '../models/charging-station';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
  connected = false;
  private mockInterval: any;

  constructor(private stationService: StationService) {}

  connect(): void {
    if (environment.production) {
      this.connectReal();
    } else {
      this.connectMock();
    }
  }

  disconnect(): void {
    if (this.mockInterval) {
      clearInterval(this.mockInterval);
    }
  }

  private connectReal(): void {
    import('@stomp/stompjs').then(({ Client }) => {
      import('sockjs-client').then(({ default: SockJS }) => {
        const client = new Client({
          webSocketFactory: () => new SockJS(environment.wsUrl),
          reconnectDelay: 5000,
          onConnect: () => {
            this.connected = true;
            console.log('✅ WebSocket connected');
            client.subscribe('/topic/status', (msg) => {
              const event: StatusChangeEvent = JSON.parse(msg.body);
              this.handleStatusChange(event);
            });
          },
          onDisconnect: () => {
            this.connected = false;
            console.log('WebSocket disconnected — will retry...');
          }
        });
        client.activate();
      });
    });
  }

  private connectMock(): void {
    this.connected = true;
    console.log('🔧 Mock WebSocket connected');

    const statuses = ['AVAILABLE', 'OCCUPIED', 'FAULTY', 'UNKNOWN'];

    this.mockInterval = setInterval(() => {
      const stations = this.stationService.stations();
      if (!stations.length) return;

      const randomStation = stations[Math.floor(Math.random() * stations.length)];
      const oldStatus = randomStation.currentStatus;
      const randomStatus = statuses[Math.floor(Math.random() * statuses.length)];

      const event: StatusChangeEvent = {
        stationUuid: randomStation.id,
        oldStatus: oldStatus,
        newStatus: randomStatus,
        londonBorough: randomStation.londonBorough,
        availableConnectors: Math.floor(Math.random() * randomStation.totalConnectors),
        totalConnectors: randomStation.totalConnectors,
        changedAt: new Date().toISOString()
      };

      console.log(`🔧 Mock update: ${event.stationUuid} → ${event.newStatus}`);
      this.handleStatusChange(event);
    }, 5000);
  }

  private handleStatusChange(event: StatusChangeEvent): void {
    console.log(`⚡ Live update: ${event.stationUuid} → ${event.newStatus}`);
    this.stationService.updateStationStatus(
      event.stationUuid,
      event.newStatus,
      event.availableConnectors
    );
  }
}