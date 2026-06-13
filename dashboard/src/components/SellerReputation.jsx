import React, { useState } from 'react'

export default function SellerReputation() {
  const [sellerId, setSellerId] = useState('seller_1')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)

  const lookup = async () => {
    setLoading(true)
    try {
      const res = await fetch(`/api/sellers/${sellerId}/reputation`)
      setData(await res.json())
    } catch (e) {
      setData({ error: 'Lookup failed' })
    } finally {
      setLoading(false)
    }
  }

  const TIER_COLOR = { TRUSTED: 'text-emerald-400', STANDARD: 'text-yellow-400', AT_RISK: 'text-red-400' }
  const score = data?.reputationScore ?? 0
  const pct = Math.round(score * 100)

  return (
    <div className="max-w-md">
      <h2 className="text-xl font-semibold mb-4">Seller Reputation</h2>
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-5">
        <div className="flex gap-2">
          <input className="flex-1 bg-slate-700 rounded px-3 py-2 text-sm" placeholder="Seller ID" value={sellerId} onChange={e => setSellerId(e.target.value)} />
          <button onClick={lookup} disabled={loading} className="bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-sm px-4 py-2 rounded">
            {loading ? '...' : 'Lookup'}
          </button>
        </div>

        {data && !data.error && (
          <div className="mt-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-slate-400">Reputation Score</span>
              <span className={`font-bold text-lg ${TIER_COLOR[data.tier] || 'text-slate-200'}`}>{pct}%</span>
            </div>
            <div className="w-full bg-slate-700 rounded-full h-2 mb-3">
              <div
                className={`h-2 rounded-full transition-all ${score > 0.7 ? 'bg-emerald-500' : score > 0.4 ? 'bg-yellow-500' : 'bg-red-500'}`}
                style={{ width: `${pct}%` }}
              />
            </div>
            <div className="flex items-center gap-2">
              <span className={`text-sm font-semibold ${TIER_COLOR[data.tier]}`}>{data.tier}</span>
              <span className="text-xs text-slate-500">seller tier</span>
            </div>
          </div>
        )}
        {data?.error && <p className="mt-3 text-sm text-red-400">{data.error}</p>}
      </div>
    </div>
  )
}
