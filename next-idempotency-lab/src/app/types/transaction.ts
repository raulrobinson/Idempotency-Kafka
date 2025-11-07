export interface Transaction {
    id: string;
    idemKey: string;
    amount: number;
    status: string;
    attempts?: number;
    payload?: Record<string, any>;
}
