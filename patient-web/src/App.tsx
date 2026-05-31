import { useEffect, useRef } from 'react'
import { motion, useReducedMotion } from 'framer-motion'
import { 
  ArrowRight, 
  ChevronDown, 
  Menu, 
  ShieldPlus, 
  Users, 
  Calendar, 
  FileText, 
  BarChart2, 
  UserCheck, 
  CreditCard, 
  Lock, 
  Cpu, 
  Activity, 
  Database, 
  Shield, 
  Terminal, 
  ArrowUpRight, 
  ExternalLink,
  Globe
} from 'lucide-react'
import { CurvedStrip } from './components/CurvedStrip'
import './App.css'

const navItems = ['Features', 'Modules', 'Technology', 'Security', 'About']

function App() {
  const heroRef = useRef<HTMLElement | null>(null)
  const prefersReducedMotion = useReducedMotion()

  useEffect(() => {
    const hero = heroRef.current
    if (!hero || prefersReducedMotion) {
      return
    }

    const moveHero = (event: PointerEvent) => {
      const bounds = hero.getBoundingClientRect()
      const relativeX = ((event.clientX - bounds.left) / bounds.width - 0.5) * 2
      const relativeY = ((event.clientY - bounds.top) / bounds.height - 0.5) * 2

      hero.style.setProperty('--mx', relativeX.toFixed(3))
      hero.style.setProperty('--my', relativeY.toFixed(3))
    }

    const resetHero = () => {
      hero.style.setProperty('--mx', '0')
      hero.style.setProperty('--my', '0')
    }

    window.addEventListener('pointermove', moveHero)
    hero.addEventListener('pointerleave', resetHero)

    return () => {
      window.removeEventListener('pointermove', moveHero)
      hero.removeEventListener('pointerleave', resetHero)
    }
  }, [prefersReducedMotion])

  return (
    <main className="patient-shell">
      {/* SECTION 1 — HERO SECTION */}
      <section className="patient-hero" ref={heroRef}>
        <div className="hero-scene" aria-hidden="true">
          <div className="hero-scene-canvas-wrapper">
            <div className="hero-scene-canvas-container">
              <CurvedStrip />
            </div>
          </div>
          <span className="detail-dot dot-a" />
          <span className="detail-dot dot-b" />
        </div>

        <motion.header
          className="hero-nav"
          initial={prefersReducedMotion ? undefined : { opacity: 0, y: -18 }}
          animate={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
          transition={{ duration: 0.45, ease: 'easeOut' }}
        >
          <a className="brand" href="#0">
            <span className="brand-badge" aria-hidden="true">
              <ShieldPlus size={15} strokeWidth={2.4} />
            </span>
            <span className="brand-name">PATIENTA</span>
          </a>

          <nav className="hero-links" aria-label="Main">
            {navItems.map((item) => (
              <a className="hero-link" href={`#${item.toLowerCase()}`} key={item}>
                {item}
                {item !== 'About' && <ChevronDown size={14} strokeWidth={2.2} />}
              </a>
            ))}
          </nav>

          <div className="hero-actions">
            <a href="#portal" className="ghost-button nav-ghost">
              Patient Portal
            </a>
            <a href="#dashboard" className="primary-button nav-primary">
              Management Dashboard
              <ArrowRight size={16} strokeWidth={2.2} />
            </a>
            <button type="button" className="menu-button" aria-label="Open menu">
              <Menu size={20} />
            </button>
          </div>
        </motion.header>

        <motion.div
          className="hero-content"
          initial={prefersReducedMotion ? undefined : { opacity: 0, y: 28 }}
          animate={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease: [0.2, 0.65, 0.25, 1] }}
        >
          <motion.p
            className="hero-kicker"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 12 }}
            animate={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            transition={{ duration: 0.4, delay: 0.08 }}
          >
            SECURE • SCALABLE • ENTERPRISE READY
          </motion.p>

          <motion.h1
            className="hero-heading"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 16 }}
            animate={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            transition={{ duration: 0.52, delay: 0.12 }}
          >
            Modern Healthcare
            <br />
            Management, Built for <span>Precision</span>
          </motion.h1>

          <motion.p
            className="hero-copy"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 16 }}
            animate={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            transition={{ duration: 0.44, delay: 0.2 }}
          >
            A scalable patient management ecosystem designed to simplify hospital workflows, streamline patient care, and modernize operational efficiency.
          </motion.p>

          <motion.div
            className="hero-buttons"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 16 }}
            animate={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            transition={{ duration: 0.42, delay: 0.28 }}
          >
            <a href="#portal" className="primary-button hero-primary">
              Open Patient Portal
              <ArrowRight size={18} strokeWidth={2.2} />
            </a>
            <a href="#dashboard" className="ghost-button hero-ghost">
              Management Dashboard
              <ArrowRight size={18} strokeWidth={2.2} />
            </a>
          </motion.div>
        </motion.div>
      </section>

      {/* SECTION 2 — ABOUT PRODUCT */}
      <motion.section 
        id="about" 
        className="section section-about"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="section-header">
          <p className="section-kicker">CENTRALIZED CARE PLATFORM</p>
          <h2 className="section-title">Designed for Modern Healthcare Systems</h2>
          <p className="section-desc">
            Patienta centralizes administrative, clinical, and operational workflows into a single high-performance ERP database, driving communication and eliminating organizational silos.
          </p>
        </div>

        <motion.div 
          className="about-grid" 
          initial="initial" 
          whileInView="whileInView" 
          viewport={{ once: true, margin: '-60px' }}
          variants={prefersReducedMotion ? undefined : {
            initial: {},
            whileInView: { transition: { staggerChildren: 0.08 } }
          }}
        >
          <motion.div 
            className="glass-card about-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 24 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } }
            }}
          >
            <div className="card-icon-shell">
              <Users className="card-icon" size={24} />
            </div>
            <h3>Patient First Design</h3>
            <p>Empower patients with a frictionless intake journey, direct scheduling tools, and real-time records access that puts clinical care at the forefront.</p>
          </motion.div>

          <motion.div 
            className="glass-card about-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 24 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } }
            }}
          >
            <div className="card-icon-shell">
              <Activity className="card-icon" size={24} />
            </div>
            <h3>Operational Efficiency</h3>
            <p>Unify staff rosters, scheduling matrices, billing systems, and resource tracking pipelines to remove double-bookings and optimize resource utilization.</p>
          </motion.div>

          <motion.div 
            className="glass-card about-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 24 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } }
            }}
          >
            <div className="card-icon-shell">
              <Shield className="card-icon" size={24} />
            </div>
            <h3>Secure Infrastructure</h3>
            <p>Engineered to exceed strict data protection and HIPAA-readiness, utilizing granular column-level audit access, data encryption, and robust safeguards.</p>
          </motion.div>
        </motion.div>
      </motion.section>

      {/* SECTION 3 — CORE FEATURES */}
      <motion.section 
        id="features" 
        className="section section-features"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="section-header">
          <p className="section-kicker">PLATFORM CAPABILITIES</p>
          <h2 className="section-title">Enterprise-Grade Features</h2>
          <p className="section-desc">
            A comprehensive suite of modules designed to deliver top-tier clinical operations, reliable database performance, and fluid layouts.
          </p>
        </div>

        <motion.div 
          className="features-grid" 
          initial="initial" 
          whileInView="whileInView" 
          viewport={{ once: true, margin: '-60px' }}
          variants={prefersReducedMotion ? undefined : {
            initial: {},
            whileInView: { transition: { staggerChildren: 0.06 } }
          }}
        >
          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <Users className="feature-icon" size={20} />
            <h4>Patient Management</h4>
            <p>Unified registration profiles, intake workflows, and personal demographic tracking panels.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <Calendar className="feature-icon" size={20} />
            <h4>Appointment Scheduling</h4>
            <p>Smart booking grid with automated conflict checking and real-time practitioner availability.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <FileText className="feature-icon" size={20} />
            <h4>Medical Records</h4>
            <p>HIPAA-ready electronic health records (EHR) with clinical histories and encryption.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <BarChart2 className="feature-icon" size={20} />
            <h4>Analytics Dashboard</h4>
            <p>Operational reports on clinical efficiency, bed occupancies, and consult volume statistics.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <UserCheck className="feature-icon" size={20} />
            <h4>Staff Management</h4>
            <p>Role assignments, shift schedules, and practitioner directory databases with logs.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <CreditCard className="feature-icon" size={20} />
            <h4>Billing & Operations</h4>
            <p>Flexible invoice generation, claims processing integrations, and transaction logs.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <Lock className="feature-icon" size={20} />
            <h4>Role-Based Access</h4>
            <p>Granular RBAC profiles separating administration, medical staff, and patient views.</p>
          </motion.div>

          <motion.div 
            className="glass-card feature-card"
            variants={prefersReducedMotion ? undefined : {
              initial: { opacity: 0, y: 20 },
              whileInView: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
            }}
          >
            <Cpu className="feature-icon" size={20} />
            <h4>Scalable Architecture</h4>
            <p>Built with lightweight microservices ready to deploy in containers for fast response times.</p>
          </motion.div>
        </motion.div>
      </motion.section>

      {/* SECTION 4 — PRODUCT ECOSYSTEM */}
      <motion.section 
        id="modules" 
        className="section section-ecosystem"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="section-header">
          <p className="section-kicker">ECOSYSTEM MODULES</p>
          <h2 className="section-title">Two Portals. One Integrated Core.</h2>
          <p className="section-desc">
            Dual portals designed to cater perfectly to clinical staff and patients alike, connected via a secure, shared database API framework.
          </p>
        </div>

        <div className="ecosystem-cards">
          <motion.div 
            className="glass-card ecosystem-card"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 24 }}
            whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.6, ease: 'easeOut' }}
          >
            <div className="ecosystem-card-header">
              <span className="badge badge-gold">PATIENT PORTAL</span>
              <h3>Frictionless Access for Patients</h3>
            </div>
            <p className="ecosystem-intro">Designed to give patients direct, responsive control over their own healthcare path from any device:</p>
            <ul className="ecosystem-list">
              <li>
                <span className="dot" />
                <span>View and schedule clinic appointments instantly</span>
              </li>
              <li>
                <span className="dot" />
                <span>Access digital health records, clinical prescriptions, and follow-ups</span>
              </li>
              <li>
                <span className="dot" />
                <span>Securely message clinical care specialists and request call consults</span>
              </li>
            </ul>
            <a href="#portal" className="secondary-button portal-btn">
              Explore Patient Portal
              <ArrowUpRight size={16} />
            </a>
          </motion.div>

          <motion.div 
            className="glass-card ecosystem-card"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 24 }}
            whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.6, ease: 'easeOut' }}
          >
            <div className="ecosystem-card-header">
              <span className="badge badge-emerald">MANAGEMENT PLATFORM</span>
              <h3>Power Tooling for Operations</h3>
            </div>
            <p className="ecosystem-intro">A command center tailored to clinical directors, administrative leads, and practitioners:</p>
            <ul className="ecosystem-list">
              <li>
                <span className="dot" />
                <span>Streamline entire hospital schedules and doctor shifts</span>
              </li>
              <li>
                <span className="dot" />
                <span>Analyze inpatient telemetry metrics and consult throughputs</span>
              </li>
              <li>
                <span className="dot" />
                <span>Manage complex medical record releases and billing cycles securely</span>
              </li>
            </ul>
            <a href="#dashboard" className="primary-button portal-btn">
              Launch Management Dashboard
              <ArrowUpRight size={16} />
            </a>
          </motion.div>
        </div>
      </motion.section>

      {/* SECTION 5 — WHY THIS PLATFORM */}
      <motion.section 
        className="section section-why"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="section-header">
          <p className="section-kicker">ENGINEERED CREDIBILITY</p>
          <h2 className="section-title">Architected for Healthcare Scale</h2>
          <p className="section-desc">
            We prioritize key platform metrics to guarantee absolute credibility, clinical safety, and low operational friction.
          </p>
        </div>

        <div className="why-grid">
          <div className="why-item">
            <h4 className="why-metric">99.99%</h4>
            <h5>High Availability</h5>
            <p>Active-active multi-region database failovers guarantee the ERP is online when patients need it most.</p>
          </div>
          <div className="why-item">
            <h4 className="why-metric">&lt;100ms</h4>
            <h5>API Endpoint Latency</h5>
            <p>Leveraging high-performance Redis caches and gRPC RPCs to retrieve clinical files instantly.</p>
          </div>
          <div className="why-item">
            <h4 className="why-metric">256-Bit</h4>
            <h5>E2E Encryption</h5>
            <p>All database records and medical files are protected with robust AES encryption and TLS 1.3 tunnels.</p>
          </div>
          <div className="why-item">
            <h4 className="why-metric">HIPAA</h4>
            <h5>Audit Auditing</h5>
            <p>Field-level logging and strict compliance checks provide transparent access auditing controls.</p>
          </div>
        </div>
      </motion.section>

      {/* SECTION 6 — TECHNOLOGY */}
      <motion.section 
        id="technology" 
        className="section section-tech"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="section-header">
          <p className="section-kicker">SYSTEM ARCHITECTURE</p>
          <h2 className="section-title">Modern Technical Infrastructure</h2>
          <p className="section-desc">
            A state-of-the-art framework built on modular components, ensuring high-speed rendering, low-latency APIs, and seamless container orchestration.
          </p>
        </div>

        <div className="tech-stack-container">
          <motion.div 
            className="glass-card tech-card"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 24 }}
            whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.55, ease: 'easeOut' }}
          >
            <div className="tech-card-header">
              <Globe size={18} className="tech-icon" />
              <h4>Frontend Technologies</h4>
            </div>
            <div className="tech-tags">
              <span>React 19</span>
              <span>Next.js</span>
              <span>TypeScript 6.0</span>
              <span>Vite Engine</span>
              <span>Framer Motion</span>
            </div>
            <p>Utilizes single-directional rendering, robust strict types, and reactive layout modules to produce beautiful, high-efficiency frontend layers.</p>
          </motion.div>

          <motion.div 
            className="glass-card tech-card"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 24 }}
            whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.55, ease: 'easeOut' }}
          >
            <div className="tech-card-header">
              <Terminal size={18} className="tech-icon" />
              <h4>Backend Engineering</h4>
            </div>
            <div className="tech-tags">
              <span>Node.js</span>
              <span>Microservices</span>
              <span>gRPC RPCs</span>
              <span>REST WebAPIs</span>
              <span>Protobufs</span>
            </div>
            <p>High-throughput service architecture allowing distinct billing, scheduling, and clinician systems to exchange data smoothly with minimal gRPC overhead.</p>
          </motion.div>

          <motion.div 
            className="glass-card tech-card"
            initial={prefersReducedMotion ? undefined : { opacity: 0, y: 24 }}
            whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
            viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.55, ease: 'easeOut' }}
          >
            <div className="tech-card-header">
              <Database size={18} className="tech-icon" />
              <h4>Database & Infrastructure</h4>
            </div>
            <div className="tech-tags">
              <span>PostgreSQL</span>
              <span>Redis Cluster</span>
              <span>Docker Containers</span>
              <span>Kubernetes</span>
              <span>AES-256</span>
            </div>
            <p>Robust PostgreSQL relational layers, high-speed transactional Redis memory caches, and containerized deployment packages ready for auto-scaling.</p>
          </motion.div>
        </div>
      </motion.section>

      {/* SECTION 7 — BUILDER SECTION */}
      <motion.section 
        className="section section-builder"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="glass-card builder-card">
          <div className="builder-header">
            <span className="badge badge-gold">SYSTEM DESIGNER</span>
            <h2 className="builder-title">Designed & Engineered by Mohd S.</h2>
            <p className="builder-subtitle">Senior Frontend Architect & Full-Stack Systems Engineer</p>
          </div>
          
          <div className="builder-content">
            <p className="builder-bio">
              A premier system architect specializing in mission-critical healthcare ERP infrastructures, high-throughput backend APIs, and pixel-perfect responsive designs. Mohd S. bridges modern engineering paradigms with state-of-the-art UI/UX aesthetic principles.
            </p>
            
            <div className="builder-links">
              <a href="https://github.com" target="_blank" rel="noopener noreferrer" className="builder-link-btn">
                <svg className="svg-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" />
                  <path d="M9 18c-4.51 2-5-2-7-2" />
                </svg>
                <span>GitHub Repositories</span>
                <ExternalLink size={14} />
              </a>
              <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" className="builder-link-btn">
                <svg className="svg-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z" />
                  <rect x="2" y="9" width="4" height="12" />
                  <circle cx="4" cy="4" r="2" />
                </svg>
                <span>LinkedIn Profile</span>
                <ExternalLink size={14} />
              </a>
              <a href="#0" className="builder-link-btn">
                <Globe size={18} />
                <span>Professional Portfolio</span>
                <ExternalLink size={14} />
              </a>
            </div>
          </div>
        </div>
      </motion.section>

      {/* SECTION 8 — closing CTA SECTION */}
      <motion.section 
        className="section section-closing-cta"
        initial={prefersReducedMotion ? undefined : { opacity: 0, y: 32 }}
        whileInView={prefersReducedMotion ? undefined : { opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-60px' }}
        transition={{ duration: 0.62, ease: [0.16, 1, 0.3, 1] as const }}
      >
        <div className="closing-cta-card">
          <h2 className="closing-title">Transform Healthcare Operations</h2>
          <p className="closing-desc">
            Integrate scheduling rosters, encrypted records database, and patient care access portals into a single medical ERP today.
          </p>
          <div className="closing-buttons">
            <a href="#portal" className="primary-button cta-primary">
              Access Patient Portal
              <ArrowRight size={18} />
            </a>
            <a href="#dashboard" className="secondary-button cta-secondary">
              Launch Management Portal
              <ArrowRight size={18} />
            </a>
          </div>
        </div>
      </motion.section>

      {/* SECTION 9 — FOOTER */}
      <footer id="security" className="hero-footer">
        <div className="footer-top">
          <div className="footer-brand-side">
            <a className="brand footer-brand" href="#0">
              <span className="brand-badge" aria-hidden="true">
                <ShieldPlus size={15} strokeWidth={2.4} />
              </span>
              <span className="brand-name">PATIENTA</span>
            </a>
            <p className="footer-tagline">Secure. Scalable. Enterprise Ready Healthcare ERP.</p>
          </div>

          <div className="footer-links-grid">
            <div className="footer-nav-col">
              <h5>Platform</h5>
              <a href="#about">About Core</a>
              <a href="#features">Features</a>
              <a href="#modules">Ecosystem Modules</a>
            </div>
            <div className="footer-nav-col">
              <h5>Resources</h5>
              <a href="#technology">Tech Stack</a>
              <a href="#security">Security Core</a>
              <a href="#about">Documentation</a>
            </div>
            <div className="footer-nav-col">
              <h5>Connect</h5>
              <a href="https://github.com" target="_blank" rel="noopener noreferrer">GitHub</a>
              <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer">LinkedIn</a>
              <a href="#0">Portfolio</a>
            </div>
          </div>
        </div>

        <div className="footer-bottom">
          <p className="copyright">© 2026 PATIENTA ERP System. Engineered by Mohd S. All rights reserved.</p>
          <div className="legal-links">
            <a href="#0">Terms of Use</a>
            <a href="#0">Privacy Policy</a>
            <a href="#0">HIPAA Safeguards</a>
          </div>
        </div>
      </footer>
    </main>
  )
}

export default App
