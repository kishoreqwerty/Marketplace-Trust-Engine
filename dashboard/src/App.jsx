import React, { useState } from 'react'
import ModerationQueue from './components/ModerationQueue.jsx'
import SubmitListing from './components/SubmitListing.jsx'
import SellerReputation from './components/SellerReputation.jsx'
import FraudDistribution from './components/FraudDistribution.jsx'

const TABS = ['Moderation Queue', 'Submit Listing', 'Seller Reputation', 'Analytics']

export default function App() {
  const [tab, setTab] = useState(0)

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100">
      <header className="bg-slate-800 border-b border-slate-700 px-6 py-4 flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-emerald-500 flex items-center justify-center text-sm font-bold">T</div>
        <h1 className="text-lg font-semibold">Trust Engine Dashboard</h1>
        <span className="ml-auto text-xs text-slate-400 bg-slate-700 px-2 py-1 rounded">Live</span>
      </header>

      <nav className="bg-slate-800 border-b border-slate-700 px-6 flex gap-1">
        {TABS.map((t, i) => (
          <button
            key={t}
            onClick={() => setTab(i)}
            className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
              tab === i
                ? 'border-emerald-500 text-emerald-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            {t}
          </button>
        ))}
      </nav>

      <main className="p-6 max-w-6xl mx-auto">
        {tab === 0 && <ModerationQueue />}
        {tab === 1 && <SubmitListing />}
        {tab === 2 && <SellerReputation />}
        {tab === 3 && <FraudDistribution />}
      </main>
    </div>
  )
}
