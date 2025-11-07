"use client";
import { motion } from "framer-motion";

const stages = [
    "API Service",
    "Redis Idempotency",
    "Kafka Topic",
    "Worker",
    "Retry Worker",
    "Postgres"
];

export default function FlowVisualizer({ active }: { active: string }) {
    return (
        <div className="flex justify-between items-center w-full mt-6">
            {stages.map((s, idx) => (
                <motion.div key={s}
                            className={`p-3 rounded-xl text-center w-40 text-sm ${
                                s === active ? "bg-green-500 text-white" : "bg-gray-700 text-gray-300"
                            }`}
                            animate={{ scale: s === active ? 1.15 : 1 }}
                            transition={{ duration: 0.4 }}>
                    {s}
                </motion.div>
            ))}
        </div>
    );
}
