import axios from "axios";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8081";

export const createTransaction = async (idemKey: string, payload: any) => {
    const res = await axios.post(`${API_BASE}/transactions`, payload, {
        headers: { "Idempotency-Key": idemKey },
    });
    return res.data;
};

export const fetchTransaction = async (idemKey: string) => {
    const res = await axios.get(`${API_BASE}/transactions/${idemKey}`);
    return res.data;
};
