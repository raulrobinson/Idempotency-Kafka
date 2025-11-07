"use client";
import { useState, useEffect } from "react";
import TransactionForm from "./components/TransactionForm";
import FlowVisualizer from "./components/FlowVisualizer";
import { fetchTransaction } from "@/app/lib/api";

export default function Page() {
    const [current, setCurrent] = useState("");
    const [idemKey, setIdemKey] = useState<string | null>(null);
    const [status, setStatus] = useState<string>("");

    useEffect(() => {
        if (!idemKey) return;
        const interval = setInterval(async () => {
            try {
                const tx = await fetchTransaction(idemKey);
                setStatus(tx.status);
                if (tx.status === "RECEIVED") setCurrent("Kafka Topic");
                if (tx.status === "PROCESSING") setCurrent("Worker");
                if (tx.status === "PROCESSED") setCurrent("Postgres");
            } catch (_) {}
        }, 1500);
        return () => clearInterval(interval);
    }, [idemKey]);

    return (
        <main className="min-h-screen bg-gray-800 flex flex-col items-center justify-center">
            <TransactionForm
                onSubmit={({ idemKey }) => {
                    setIdemKey(idemKey);
                    setCurrent("API Service");
                }}
            />
            {idemKey && (
                <>
                    <div className="text-gray-200 mt-5 text-sm">IdemKey: {idemKey}</div>
                    <FlowVisualizer active={current} />
                    <div className="text-gray-300 mt-4">Estado: {status}</div>
                </>
            )}
        </main>
    );
}
