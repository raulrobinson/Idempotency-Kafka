"use client";
import { useState } from "react";
import {createTransaction} from "@/app/lib/api";

export default function TransactionForm({ onSubmit }: { onSubmit: (data: any) => void }) {
    const [amount, setAmount] = useState(100);
    const [customerId, setCustomerId] = useState("C-123");
    const [note, setNote] = useState("test transfer");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async () => {
        setLoading(true);
        const idemKey = `idem-${Date.now()}`;
        try {
            const data = await createTransaction(idemKey, { amount, payload: { customerId, note } });
            onSubmit({ idemKey, data });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4 bg-gray-600 text-gray-200 rounded-xl shadow-md w-[400px]">
            <h2 className="text-xl mb-3 font-bold">Enviar Transferencia</h2>
            <label className="block mb-2">Monto:</label>
            <input className="w-full p-2 rounded text-black"
                   type="number" value={amount} onChange={(e) => setAmount(Number(e.target.value))}/>
            <label className="block mt-3 mb-2">Cliente:</label>
            <input className="w-full p-2 rounded text-black"
                   value={customerId} onChange={(e) => setCustomerId(e.target.value)}/>
            <label className="block mt-3 mb-2">Nota:</label>
            <input className="w-full p-2 rounded text-black"
                   value={note} onChange={(e) => setNote(e.target.value)}/>
            <button disabled={loading} onClick={handleSubmit}
                    className="bg-blue-600 hover:bg-blue-700 mt-4 p-2 rounded w-full">
                {loading ? "Enviando..." : "Enviar"}
            </button>
        </div>
    );
}
