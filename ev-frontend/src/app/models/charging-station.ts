export interface ChargingStation {
  id: string;
  ocmUuid: string;
  operatorName?: string;
  latitude: number;
  longitude: number;
  postcode: string;
  londonBorough: string;
  totalConnectors: number;
  availableConnectors: number;
  currentStatus: 'AVAILABLE' | 'OCCUPIED' | 'FAULTY' | 'UNKNOWN';
  lastUpdated: string;
}

export interface StatusChangeEvent {
  stationUuid: string;
  oldStatus: string;
  newStatus: string;
  londonBorough: string;
  availableConnectors: number;
  totalConnectors: number;
  changedAt: string;
}
