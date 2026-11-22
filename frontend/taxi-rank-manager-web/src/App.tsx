import { BrowserRouter, Routes, Route } from 'react-router-dom'

function Home() {
  return (
    <main style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>Taxi Rank Manager</h1>
      <p>Frontend scaffold ready. Routes and pages to be implemented.</p>
    </main>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
      </Routes>
    </BrowserRouter>
  )
}
