import React, { useState } from 'react'

const CATEGORIES = ['electronics', 'clothing', 'furniture', 'vehicles', 'jewelry', 'toys', 'sports']

export default function SubmitListing() {
  const [form, setForm] = useState({ sellerId: 'seller_1', title: '', description: '', price: '', category: 'electronics', condition: 'used', locationCity: '', locationState: '' })
  const [result, setResult] = useState(null)
  const [aiResult, setAiResult] = useState(null)
  const [loading, setLoading] = useState(false)

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const submit = async () => {
    setLoading(true)
    setResult(null)
    try {
      const res = await fetch('/api/listings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...form, price: parseFloat(form.price) })
      })
      const data = await res.json()
      setResult(data)
      // Poll for score
      setTimeout(async () => {
        const r2 = await fetch(`/api/listings/${data.id}`)
        setResult(await r2.json())
      }, 3000)
    } catch (e) {
      setResult({ error: 'Submit failed — is the listing-service running?' })
    } finally {
      setLoading(false)
    }
  }

  const aiImprove = async () => {
    setLoading(true)
    try {
      const res = await fetch(`/api/listings/00000000-0000-0000-0000-000000000000/ai-improve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...form, price: parseFloat(form.price) })
      })
      setAiResult(await res.json())
    } catch (e) {
      setAiResult({ error: 'AI improve failed' })
    } finally {
      setLoading(false)
    }
  }

  const LABEL_COLOR = { CLEAN: 'text-emerald-400', REVIEW: 'text-yellow-400', FLAGGED: 'text-red-400', PENDING: 'text-slate-400' }

  return (
    <div className="max-w-2xl">
      <h2 className="text-xl font-semibold mb-4">Submit Test Listing</h2>
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-5 space-y-3">
        <div className="grid grid-cols-2 gap-3">
          <input className="bg-slate-700 rounded px-3 py-2 text-sm col-span-2" placeholder="Seller ID (e.g. seller_1)" value={form.sellerId} onChange={e => set('sellerId', e.target.value)} />
          <input className="bg-slate-700 rounded px-3 py-2 text-sm col-span-2" placeholder="Title" value={form.title} onChange={e => set('title', e.target.value)} />
          <textarea className="bg-slate-700 rounded px-3 py-2 text-sm col-span-2 h-20 resize-none" placeholder="Description" value={form.description} onChange={e => set('description', e.target.value)} />
          <input className="bg-slate-700 rounded px-3 py-2 text-sm" placeholder="Price ($)" type="number" value={form.price} onChange={e => set('price', e.target.value)} />
          <select className="bg-slate-700 rounded px-3 py-2 text-sm" value={form.category} onChange={e => set('category', e.target.value)}>
            {CATEGORIES.map(c => <option key={c}>{c}</option>)}
          </select>
          <input className="bg-slate-700 rounded px-3 py-2 text-sm" placeholder="City" value={form.locationCity} onChange={e => set('locationCity', e.target.value)} />
          <input className="bg-slate-700 rounded px-3 py-2 text-sm" placeholder="State" value={form.locationState} onChange={e => set('locationState', e.target.value)} />
        </div>
        <div className="flex gap-2 pt-1">
          <button onClick={submit} disabled={loading} className="bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-sm px-4 py-2 rounded font-medium">
            {loading ? 'Submitting...' : 'Submit Listing'}
          </button>
          <button onClick={aiImprove} disabled={loading || !form.title} className="bg-indigo-700 hover:bg-indigo-600 disabled:opacity-50 text-white text-sm px-4 py-2 rounded font-medium">
            ✨ AI Improve
          </button>
        </div>
      </div>

      {result && !result.error && (
        <div className="mt-4 bg-slate-800 border border-slate-700 rounded-lg p-4">
          <p className="text-sm font-medium mb-2">Result</p>
          <div className="text-xs text-slate-400 space-y-1">
            <p>ID: <span className="text-slate-200 font-mono">{result.id}</span></p>
            <p>Status: <span className={`font-bold ${LABEL_COLOR[result.trustLabel] || 'text-slate-300'}`}>{result.trustLabel}</span>
              {result.fraudScore != null && <span className="text-slate-400"> ({(result.fraudScore*100).toFixed(1)}% fraud probability)</span>}
              {result.trustLabel === 'PENDING' && <span className="text-slate-500"> — scoring in progress...</span>}
            </p>
            {result.flaggedReason && <p>Reason: <span className="text-red-400">{result.flaggedReason}</span></p>}
          </div>
        </div>
      )}

      {aiResult && (
        <div className="mt-4 bg-slate-800 border border-indigo-800 rounded-lg p-4">
          <p className="text-sm font-medium text-indigo-400 mb-2">✨ AI Suggestions</p>
          {aiResult.error ? (
            <p className="text-xs text-red-400">{aiResult.error}</p>
          ) : (
            <div className="text-xs space-y-2">
              {aiResult.improvedTitle && <p><span className="text-slate-400">Improved title:</span> <span className="text-slate-200">{aiResult.improvedTitle}</span></p>}
              {aiResult.pricingSuggestion && <p><span className="text-slate-400">Pricing:</span> <span className="text-slate-200">{aiResult.pricingSuggestion}</span></p>}
              {aiResult.riskFlags?.length > 0 && (
                <p><span className="text-red-400">Risk flags:</span> {aiResult.riskFlags.join(', ')}</p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
