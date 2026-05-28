import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "New Long-term Goal — Decision Echo" },
      { name: "description", content: "Create a new long-term goal in Decision Echo." },
      {
        rel: "stylesheet",
        href: "https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap",
      } as never,
    ],
    links: [
      {
        rel: "stylesheet",
        href: "https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap",
      },
      {
        rel: "stylesheet",
        href: "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap",
      },
    ],
  }),
  component: NewGoalPage,
});

const navItems = [
  { icon: "home", label: "Home", active: false },
  { icon: "bar_chart", label: "Stats", active: false },
  { icon: "flag", label: "Long-term Goals", active: true },
  { icon: "person", label: "Mine", active: false },
];

function Icon({ name, filled = false, className = "" }: { name: string; filled?: boolean; className?: string }) {
  return (
    <span
      className={`material-symbols-outlined ${className}`}
      style={{
        fontFamily: "'Material Symbols Outlined', sans-serif",
        fontSize: "24px",
        lineHeight: 1,
        display: "inline-block",
        fontVariationSettings: filled ? "'FILL' 1" : undefined,
      }}
    >
      {name}
    </span>
  );
}

function NewGoalPage() {
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState("not_started");

  return (
    <div className="flex h-screen overflow-hidden bg-background text-on-background">
      {/* Side Nav */}
      <nav className="bg-surface-container-lowest shadow-sm h-screen w-64 fixed left-0 top-0 flex flex-col py-lg border-r border-outline-variant z-50">
        <div className="px-lg mb-xl">
          <h1 className="text-headline-sm text-primary font-semibold flex items-center gap-sm">
            <Icon name="graphic_eq" filled />
            Decision Echo
          </h1>
        </div>
        <ul className="flex-1 space-y-sm">
          {navItems.map((item) => (
            <li key={item.label}>
              <a
                href="#"
                className={`flex items-center gap-md mx-md px-md py-sm rounded-full transition-all ${
                  item.active
                    ? "bg-primary-container text-on-primary-container font-bold"
                    : "text-on-surface-variant hover:text-primary hover:bg-surface-container-high"
                }`}
              >
                <Icon name={item.icon} filled={item.active} />
                <span className="text-label-md">{item.label}</span>
              </a>
            </li>
          ))}
        </ul>
      </nav>

      {/* Main */}
      <main
        className="ml-64 flex-1 flex flex-col h-screen relative"
        style={{ background: "linear-gradient(to bottom right, var(--primary-fixed), var(--secondary-fixed))" }}
      >
        {/* Header */}
        <header className="h-16 flex items-center justify-between px-lg z-40 bg-white/80 backdrop-blur-md border-b border-white/20">
          <div className="flex-1 max-w-2xl relative">
            <Icon
              name="search"
              className="absolute left-sm top-1/2 -translate-y-1/2 text-on-surface-variant"
            />
            <input
              type="text"
              placeholder="Search goals, decisions, tags..."
              className="w-full bg-surface-container-low border-none rounded-full py-sm pl-xl pr-md text-body-md text-on-surface focus:ring-2 focus:ring-primary focus:bg-white outline-none transition-all placeholder:text-on-surface-variant/70"
            />
          </div>
          <div className="flex items-center gap-md ml-lg">
            <button className="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center text-on-surface-variant hover:text-primary hover:bg-white transition-colors">
              <Icon name="logout" />
            </button>
            <img
              alt="User avatar"
              className="w-10 h-10 rounded-full border-2 border-white shadow-sm object-cover"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuAh674D6egR2NY7sASi1wuhWlchTkewyElTpCKACebUSQqhre6Sik6v7NRijVv6V6nDNfWEQ6cfkO5f_j5F0cdjGYVf8RKeyNRFHlwoC_h7H4JAEXTkmkCzLfuLvPA8MksuCNUMYntEnn4LZxHlgaMzeJwnLBKnpO2DJC6blTIk_dBP9Q6NtUuBCqlMYTNzG887b0USiRN3ZATmnp8yr5w2LGyYE7UeLp7BEhmopwGBRDDzWecLVNNlHPQO9ZzdVhbkDaah85CTz0M"
            />
          </div>
        </header>

        {/* Form */}
        <div className="flex-1 overflow-y-auto p-lg flex items-start justify-center">
          <div className="bg-surface-container-lowest/90 backdrop-blur-xl w-full max-w-3xl rounded-xl border border-white/50 p-xl relative overflow-hidden" style={{ boxShadow: "0 8px 30px rgb(0 0 0 / 0.04)" }}>
            <div
              className="absolute top-0 right-0 w-64 h-64 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none"
              style={{ backgroundColor: "color-mix(in oklab, var(--primary-fixed) 30%, transparent)" }}
            />
            <div className="relative z-10">
              <div className="mb-lg border-b border-outline-variant/30 pb-md flex justify-between items-center">
                <h2 className="text-headline-md font-bold text-on-surface">New Long-term Goal</h2>
                <span className="text-primary bg-primary-container p-sm rounded-full inline-flex">
                  <Icon name="flag_circle" />
                </span>
              </div>

              <form className="space-y-lg" onSubmit={(e) => e.preventDefault()}>
                <Field label="Goal Title">
                  <input type="text" placeholder="e.g., Get Senior Engineer Certificate" className={inputCls} />
                </Field>
                <Field label="Goal Description">
                  <textarea rows={3} placeholder="Describe the result you want to achieve..." className={`${inputCls} resize-none`} />
                </Field>

                <div className="grid grid-cols-2 gap-lg">
                  <Field label="Category">
                    <SelectWrap>
                      <select className={`${inputCls} appearance-none pr-xl`} defaultValue="">
                        <option disabled value="">Select category</option>
                        <option value="learning">Learning</option>
                        <option value="work">Work</option>
                        <option value="health">Health</option>
                        <option value="finance">Finance</option>
                      </select>
                    </SelectWrap>
                  </Field>
                  <Field label="Priority">
                    <SelectWrap>
                      <select className={`${inputCls} appearance-none pr-xl`} defaultValue="">
                        <option disabled value="">Select priority</option>
                        <option value="high">High</option>
                        <option value="medium">Medium</option>
                        <option value="low">Low</option>
                      </select>
                    </SelectWrap>
                  </Field>
                </div>

                <div className="grid grid-cols-2 gap-lg">
                  <Field label="Expected Completion Date">
                    <input type="date" className={`${inputCls} text-on-surface-variant`} />
                  </Field>
                  <Field label="Measurement">
                    <input type="text" placeholder="e.g., Score over 90 in the exam" className={inputCls} />
                  </Field>
                </div>

                <div>
                  <div className="flex justify-between mb-xs">
                    <label className="text-label-md text-on-surface-variant">Initial Progress</label>
                    <span className="text-label-md text-primary font-bold">{progress}%</span>
                  </div>
                  <input
                    type="range"
                    min={0}
                    max={100}
                    value={progress}
                    onChange={(e) => setProgress(Number(e.target.value))}
                    className="w-full h-2 bg-surface-container-highest rounded-lg appearance-none cursor-pointer accent-primary"
                  />
                </div>

                <div>
                  <label className="block text-label-md text-on-surface-variant mb-sm">Current Status</label>
                  <div className="flex flex-wrap gap-sm">
                    {[
                      { v: "not_started", l: "Not Started", cls: "bg-primary-container border-primary-container text-on-primary-container" },
                      { v: "in_progress", l: "In Progress", cls: "bg-secondary-fixed border-secondary-fixed text-on-secondary-fixed" },
                      { v: "completed", l: "Completed", cls: "bg-tertiary-fixed border-tertiary-fixed text-on-tertiary-fixed" },
                      { v: "abandoned", l: "Abandoned", cls: "bg-surface-variant border-surface-variant text-on-surface-variant" },
                    ].map((s) => {
                      const selected = status === s.v;
                      return (
                        <label key={s.v} className="cursor-pointer">
                          <input type="radio" name="status" value={s.v} checked={selected} onChange={() => setStatus(s.v)} className="sr-only" />
                          <div
                            className={`px-md py-xs rounded-full border text-body-md transition-all ${
                              selected ? `${s.cls} font-bold` : "border-outline-variant text-on-surface-variant"
                            }`}
                          >
                            {s.l}
                          </div>
                        </label>
                      );
                    })}
                  </div>
                </div>

                <div className="flex justify-end gap-md pt-md border-t border-outline-variant/30 mt-xl">
                  <button
                    type="button"
                    className="px-lg py-sm rounded-full text-label-md text-primary border border-outline-variant hover:bg-surface-container-low hover:border-primary transition-all"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-lg py-sm rounded-full text-label-md text-on-primary bg-primary transition-all hover:-translate-y-0.5"
                    style={{ boxShadow: "0 4px 10px rgba(158,58,104,0.3)" }}
                  >
                    Save Goal
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

const inputCls =
  "w-full bg-surface border border-outline-variant/50 rounded-lg py-sm px-md text-body-md text-on-surface focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all";

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-label-md text-on-surface-variant mb-xs">{label}</label>
      {children}
    </div>
  );
}

function SelectWrap({ children }: { children: React.ReactNode }) {
  return (
    <div className="relative">
      {children}
      <Icon name="expand_more" className="absolute right-sm top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none" />
    </div>
  );
}
