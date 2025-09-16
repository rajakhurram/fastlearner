export interface Notification {
    id?: number;
    type?: string;
    url?: string;
    content?: string;
    contentType?: string;
    senderName?: string;
    senderImageURL?: string;
    receiverIds?: RecieverId[];
    creationDate?: string;
    read?: boolean;
    version?: number;
    timeInterval?: string;
  }
  
  export interface RecieverId {
    id?: any;
  }
  