import React from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'

const MOCK_DATA = [
  { range: '0-10%', count: 4120, color: '#10b981' },
  { range: '10-20%', count: 2840, color: '#10b981' },
  { range: '20-30%', count: 1560, color: '#10b981' },
  { range: '30-40%', count: 920, color: '#10b981' },
  { range: '40-50%', count: 610, color: '#eab308' },
  { range: '50-60%', count: 380, color: '#eab308' },
  { range: '60-70%', count: 210, color: '#f97316' },
  { range: '70-80%', count: 140, color: '#ef4444' },
  { range: '80-90%', count: 85, color: '#ef4444' },
  { range: '90-100%', count: 45, color: '#ef4444' },
]

const CATEGORY_DATA = [
  { category: 'Electronics', fraudRate: 8.2 },
  { category: 'Vehicles', fraudRate: 12.7 },
  { category: 'Jewelry', fraudRate: 15.1 },
  { category: 'Clothing', fraudRate: 3.4 },
  { category: 'Furniture', fraudRate: 4.8 },
  { category: 'Sports', fraudRate: 2.9 },
]

export default function FraudDistribution() {
  return (
    <div>
      <h2 className="text-xl font-semibold mb-5">Analytics (Mock Data)</h2>
      <div className="grid grid-cols-3 gap-4 mb-6">
        {[
          { label: 'Listings Scored', value: '10,910', delta: '+234 today' },
          { label: 'Flagged Rate', value: '4.1%', delta: '-0.3% vs last week' },
          { label: 'Avg Fraud Score', value: '0.18', delta: 'Low risk baseline' },
        ].map(s => (
          <div key={s.label} className="bg-slate-800 border border-slate-700 rounded-lg p-4">
            <p className="text-xs text-slate-400 mb-1">{s.label}</p>
            <p className="text-2xl font-bold">{s.value}</p>
            <p className="text-xs text-slate-500 mt-1">{s.delta}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="bg-slate-800 border border-slate-700 rounded-lg p-4">
          <p className="text-sm font-medium mb-4">Fraud Score Distribution</p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={MOCK_DATA} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
              <XAxis dataKey="range" tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <YAxis tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 6 }} />
              <Bar dataKey="count" radius={[3,3,0,0]}>
                {MOCK_DATA.map((d, i) => <Cell key={i} fill={d.color} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-slate-800 border border-slate-700 rounded-lg p-4">
          <p className="text-sm font-medium mb-4">Fraud Rate by Category</p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={CATEGORY_DATA} layout="vertical" margin={{ top: 0, right: 10, left: 30, bottom: 0 }}>
              <XAxis type="number" tick={{ fontSize: 10, fill: '#94a3b8' }} unit="%" />
              <YAxis dataKey="category" type="category" tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 6 }} formatter={(v) => [`${v}%`, 'Fraud Rate']} />
              <Bar dataKey="fraudRate" fill="#f97316" radius={[0,3,3,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
