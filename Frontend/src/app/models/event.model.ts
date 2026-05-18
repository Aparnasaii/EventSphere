export interface Event {
  eventId:        number;
  name:           string;
  description:    string;
  category:       string;
  location:       string;
  status:         string;         // 'ACTIVE' | 'CANCELLED' | 'COMPLETED'
  eventDate:      string;         // LocalDate → "yyyy-MM-dd"
  startTime:      string;         // LocalDateTime → "yyyy-MM-ddTHH:mm:ss"
  endTime:        string;         // LocalDateTime → "yyyy-MM-ddTHH:mm:ss"
  ticketPrice:    number;
  totalCapacity:  number;
  availableSeats: number;
  organizerId:    number;
  image?:         string;
  createdAt?:     string;
  updatedAt?:     string;
}

export interface EventSearchParams {
  category?: string;
  location?: string;
  date?:     string;
}
