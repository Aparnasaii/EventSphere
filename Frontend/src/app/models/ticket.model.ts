export interface Ticket {
  ticketId:    number;
  userId:      number;
  eventId:     number;
  status:      TicketStatus;
  quantity:    number;
  price:       number;
  bookingDate: string;   // matches backend field name exactly
}

export interface BookTicketRequest {
  userId:   number;
  eventId:  number;
  quantity: number;      // REQUIRED — backend throws NPE without this
}

// Matches backend TicketBookingResponseDTO exactly
export interface TicketBookingResponse {
  ticketId:    number;
  status:      string;
  checkoutUrl: string | null;   // null for free events
}

// TicketStatus mirrors the backend enum exactly
export enum TicketStatus {
  PENDING_PAYMENT = 'PENDING_PAYMENT',
  CONFIRMED       = 'CONFIRMED',
  CANCELLED       = 'CANCELLED',
  FAILED          = 'FAILED'
}
