import React, { useEffect, useRef } from 'react'

interface Point {
  x: number
  y: number
}

export const CurvedStrip: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    if (!ctx) return

    let animationId: number
    let width = 0
    let height = 0

    // Set up canvas size and scaling
    const resize = () => {
      const dpr = window.devicePixelRatio || 1
      width = canvas.clientWidth
      height = canvas.clientHeight
      canvas.width = width * dpr
      canvas.height = height * dpr
      ctx.scale(dpr, dpr)
    }

    resize()
    window.addEventListener('resize', resize)

    // Number of sampled points along the spine of each ribbon
    const numPoints = 75
    
    // Initialize vertex arrays for 3 distinct ribbons
    const ribbon1Points = Array.from({ length: numPoints }, () => ({ x: 0, y: 0 } as Point))
    const ribbon2Points = Array.from({ length: numPoints }, () => ({ x: 0, y: 0 } as Point))
    const ribbon3Points = Array.from({ length: numPoints }, () => ({ x: 0, y: 0 } as Point))

    const render = () => {
      ctx.clearRect(0, 0, width, height)

      // Slow, majestic waving time multiplier
      const time = Date.now() * 0.00028

      // Mathematical sine wave parameters
      const waveFreq = 1.85 // Extremely broad, elegant waves (1.85 cycles)
      const waveAmp = height * 0.08 // Perfectly balanced wave displacement height

      // Point updates using a pure mathematical horizontal traveling sine wave equation
      const updateRibbonPoints = (points: Point[], phaseOffset: number, ambientAmp: number) => {
        for (let i = 0; i < numPoints; i++) {
          const t = i / (numPoints - 1)

          // 1. Pure horizontal coordinate sweep
          const x = -width * 0.15 + t * (width * 1.3)

          // 2. Sloped center baseline: sloping downwards to force the wave to fill the bottom-right corner
          const centerY = height * 0.81 + t * (height * 0.13)

          // 3. Pure traveling harmonic sine wave equation
          const baseSine = Math.sin(t * Math.PI * waveFreq - time * 0.85 + phaseOffset)
          const y = centerY + baseSine * waveAmp

          // 4. Subtle secondary wave oscillation to add organic depth
          const secondaryWave = Math.cos(t * Math.PI * 1.5 + time * 0.5 + phaseOffset) * ambientAmp * 0.25

          points[i].x = x
          points[i].y = y + secondaryWave
        }
      }

      // Update all three layers with unique phase offsets
      updateRibbonPoints(ribbon1Points, 0, 15) // Deep emerald shadow
      updateRibbonPoints(ribbon2Points, Math.PI * 0.35, 10) // Active middle teal
      updateRibbonPoints(ribbon3Points, Math.PI * 0.7, 5) // Glass specular core

      // Helper to draw a solid filled ribbon with variable thickness
      const drawRibbon = (
        points: Point[],
        thickness: number,
        fillGradient: CanvasGradient,
        strokeColor?: string,
        strokeWidth?: number
      ) => {
        const leftSide: { x: number; y: number }[] = []
        const rightSide: { x: number; y: number }[] = []

        for (let i = 0; i < numPoints; i++) {
          const pt = points[i]
          
          // Tangent calculation for perpendicular normal vectors
          let tx = 0
          let ty = 0
          if (i === 0) {
            tx = points[1].x - points[0].x
            ty = points[1].y - points[0].y
          } else if (i === numPoints - 1) {
            tx = points[numPoints - 1].x - points[numPoints - 2].x
            ty = points[numPoints - 1].y - points[numPoints - 2].y
          } else {
            tx = points[i + 1].x - points[i - 1].x
            ty = points[i + 1].y - points[i - 1].y
          }

          const len = Math.sqrt(tx * tx + ty * ty) || 1
          const nx = -ty / len // Normal vector x
          const ny = tx / len  // Normal vector y

          // Taper thickness: expanded starting envelope (0.48 + 0.52 * sin) to prevent zero-tapering at the start/end
          const taper = 0.48 + 0.52 * Math.sin((i / (numPoints - 1)) * Math.PI)
          const currentThickness = thickness * taper

          leftSide.push({
            x: pt.x + nx * (currentThickness * 0.5),
            y: pt.y + ny * (currentThickness * 0.5)
          })
          rightSide.push({
            x: pt.x - nx * (currentThickness * 0.5),
            y: pt.y - ny * (currentThickness * 0.5)
          })
        }

        // Draw ribbon contour
        ctx.beginPath()
        ctx.moveTo(leftSide[0].x, leftSide[0].y)
        for (let i = 1; i < numPoints; i++) {
          ctx.lineTo(leftSide[i].x, leftSide[i].y)
        }
        for (let i = numPoints - 1; i >= 0; i--) {
          ctx.lineTo(rightSide[i].x, rightSide[i].y)
        }
        ctx.closePath()

        ctx.fillStyle = fillGradient
        ctx.fill()

        // Specular edge stroke rendering
        if (strokeColor && strokeWidth) {
          ctx.lineWidth = strokeWidth
          ctx.strokeStyle = strokeColor
          ctx.stroke()
        }
      }

      // Create rich premium gradients matching the LUMINA style palette
      const grad1 = ctx.createLinearGradient(0, height, width, 0)
      grad1.addColorStop(0, 'rgba(4, 82, 74, 0.08)')
      grad1.addColorStop(0.3, 'rgba(10, 117, 106, 0.35)')
      grad1.addColorStop(0.7, 'rgba(5, 84, 76, 0.44)')
      grad1.addColorStop(1, 'rgba(74, 186, 172, 0.12)')

      const grad2 = ctx.createLinearGradient(0, height * 0.9, width * 0.9, 0)
      grad2.addColorStop(0, 'rgba(10, 117, 106, 0.15)')
      grad2.addColorStop(0.4, 'rgba(74, 186, 172, 0.38)')
      grad2.addColorStop(0.8, 'rgba(10, 117, 106, 0.32)')
      grad2.addColorStop(1, 'rgba(255, 255, 255, 0.08)')

      const grad3 = ctx.createLinearGradient(0, height * 0.8, width * 0.8, 0)
      grad3.addColorStop(0, 'rgba(255, 255, 255, 0.0)')
      grad3.addColorStop(0.3, 'rgba(255, 255, 255, 0.42)')
      grad3.addColorStop(0.65, 'rgba(74, 186, 172, 0.48)')
      grad3.addColorStop(1, 'rgba(255, 255, 255, 0.12)')

      // Draw the layers bottom to top (medium wide widths to balance premium looks with layout safety)
      drawRibbon(ribbon1Points, 300, grad1) // Broad base emerald wave
      drawRibbon(ribbon2Points, 190, grad2) // Middle active teal
      drawRibbon(ribbon3Points, 72, grad3, 'rgba(255, 255, 255, 0.48)', 1.2) // Specular highlight core

      animationId = requestAnimationFrame(render)
    }

    render()

    return () => {
      cancelAnimationFrame(animationId)
      window.removeEventListener('resize', resize)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      style={{
        width: '100%',
        height: '100%',
        display: 'block',
        pointerEvents: 'none'
      }}
    />
  )
}
