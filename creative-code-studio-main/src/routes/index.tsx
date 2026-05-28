import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "DecideFlow Pro — Dashboard" },
      { name: "description", content: "Track, review, and learn from your decisions." },
      { property: "og:title", content: "DecideFlow Pro — Dashboard" },
      { property: "og:description", content: "Track, review, and learn from your decisions." },
    ],
  }),
  component: Index,
});

const Icon = ({ name, className = "", filled = false }: { name: string; className?: string; filled?: boolean }) => (
  <span
    className={`material-symbols-outlined ${className}`}
    style={filled ? { fontVariationSettings: "'FILL' 1" } : undefined}
  >
    {name}
  </span>
);

const navItems = [
  { icon: "dashboard", label: "Dashboard", active: true },
  { icon: "account_tree", label: "Decisions" },
  { icon: "analytics", label: "Metrics" },
  { icon: "history", label: "History" },
  { icon: "group", label: "Team" },
];

function Index() {
  return (
    <div className="flex h-screen overflow-hidden bg-df-surface text-df-on-surface font-jakarta text-[14px]">
      {/* Side nav */}
      <nav className="bg-df-surface-container-lowest h-screen w-64 fixed left-0 top-0 shadow-sm flex flex-col py-lg border-r border-df-outline-variant z-50">
        <div className="px-lg mb-xl flex items-center gap-sm">
          <div className="w-10 h-10 rounded-xl bg-df-primary-container flex items-center justify-center text-df-primary shadow-sm shrink-0">
            <Icon name="insights" filled />
          </div>
          <div>
            <h1 className="text-[20px] font-semibold leading-7 text-df-primary">DecideFlow Pro</h1>
            <p className="text-[11px] font-medium text-df-on-surface-variant">Enterprise Plan</p>
          </div>
        </div>

        <div className="flex-1 flex flex-col gap-xs overflow-y-auto">
          {navItems.map((item) => (
            <a
              key={item.label}
              href="#"
              className={`flex items-center gap-md mx-md px-md py-sm rounded-full transition-all active:scale-[0.98] ${
                item.active
                  ? "bg-df-primary-container text-df-on-primary-container font-bold"
                  : "text-df-on-surface-variant hover:text-df-primary hover:bg-df-surface-container-high"
              }`}
            >
              <Icon name={item.icon} />
              <span className="text-[12px] font-semibold tracking-wide">{item.label}</span>
            </a>
          ))}
        </div>

        <div className="px-md mt-auto">
          <button className="w-full bg-df-primary text-df-on-primary rounded-full py-sm px-md flex items-center justify-center gap-sm text-[12px] font-semibold shadow-[0_4px_20px_rgba(0,0,0,0.05)] hover:bg-df-on-primary-fixed-variant transition-colors active:scale-[0.98]">
            <Icon name="add" className="text-[18px]" />
            New Decision
          </button>
        </div>
      </nav>

      {/* Main */}
      <div className="flex-1 ml-64 flex flex-col h-screen">
        {/* Top bar */}
        <header className="bg-df-surface shadow-[0_4px_20px_rgba(0,0,0,0.05)] w-full h-16 flex justify-between items-center px-lg sticky top-0 z-40">
          <div className="relative w-96">
            <Icon name="search" className="absolute left-sm top-1/2 -translate-y-1/2 text-df-outline" />
            <input
              type="text"
              placeholder="Search decisions, tags..."
              className="w-full h-10 pl-10 pr-sm bg-df-surface-container-lowest border border-df-outline-variant rounded-full text-[14px] text-df-on-surface focus:outline-none focus:border-df-primary focus:ring-1 focus:ring-df-primary transition-all"
            />
          </div>
          <div className="flex items-center gap-sm">
            {["notifications", "help", "settings"].map((i) => (
              <button
                key={i}
                className="w-10 h-10 rounded-full flex items-center justify-center text-df-on-surface-variant hover:bg-df-surface-container-low transition-colors active:scale-95"
              >
                <Icon name={i} />
              </button>
            ))}
            <div className="w-10 h-10 ml-sm rounded-full bg-df-surface-variant overflow-hidden border border-df-outline-variant cursor-pointer">
              <img
                alt="User profile"
                className="w-full h-full object-cover"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuAYkE7DiBnymGukAqvnWvu-thmpU9AmPQaZhte0P7v9Oiaax933t3GY0Ut587xmjxMZP4KSAec7jILaw2QmB4AKqb3Jjkct_ETRuvCN43jL8gXA51razQdmF-aMaEF8j12RGS2eZsvAuUgz5qrZCDR-c7apxPU3LNh7a2vZLDd5gdy2U9bLCL1dwAGMhPMkaYQpGGa6f068qR8B5ulmdy5ZACuwY-8cVKPSMZHluR6wLY-71AmTDtU3igfVv7DQt2dFrXTRi0ydv7s"
              />
            </div>
          </div>
        </header>

        {/* Content */}
        <div className="flex flex-1 overflow-hidden">
          <main className="flex-1 overflow-y-auto p-lg bg-df-surface">
            <div className="max-w-4xl mx-auto space-y-lg">
              {/* Stats */}
              <section className="grid grid-cols-3 gap-md">
                {[
                  { label: "总决策数", value: "128", accent: "bg-df-primary-container", trend: <span className="text-df-primary text-sm flex items-center"><Icon name="trending_up" className="text-[16px]" /> 12%</span> },
                  { label: "待回看", value: "15", accent: "bg-df-secondary-container", trend: <span className="text-df-outline text-sm">本周需处理</span> },
                  { label: "已复盘", value: "84", accent: "bg-df-tertiary-container", trend: <span className="text-df-outline text-sm">命中率 76%</span> },
                ].map((s) => (
                  <div key={s.label} className="bg-df-surface-container-lowest p-md rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.05)] border border-df-outline-variant/30 flex flex-col justify-between h-28 relative overflow-hidden">
                    <div className={`absolute top-0 right-0 w-16 h-16 rounded-bl-full opacity-20 ${s.accent}`}></div>
                    <span className="text-[12px] font-semibold text-df-on-surface-variant">{s.label}</span>
                    <div className="flex items-baseline gap-sm">
                      <span className="text-[32px] font-bold leading-10 tracking-tight text-df-on-surface">{s.value}</span>
                      {s.trend}
                    </div>
                  </div>
                ))}
              </section>

              {/* Filters */}
              <section className="flex justify-between items-center bg-df-surface-container-lowest p-sm rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.05)]">
                <div className="flex gap-sm">
                  <button className="px-md py-sm rounded-full bg-df-primary text-df-on-primary text-[12px] font-semibold shadow-sm">全部</button>
                  <button className="px-md py-sm rounded-full bg-df-surface-container-low text-df-on-surface-variant hover:bg-df-surface-variant text-[12px] font-semibold transition-colors">待回看</button>
                  <button className="px-md py-sm rounded-full bg-df-surface-container-low text-df-on-surface-variant hover:bg-df-surface-variant text-[12px] font-semibold transition-colors">已复盘</button>
                </div>
                <button className="flex items-center gap-xs px-md py-sm rounded-full border border-df-outline-variant text-df-on-surface text-[12px] font-semibold hover:bg-df-surface-container-low transition-colors">
                  <Icon name="filter_list" className="text-[18px]" />
                  全部标签
                </button>
              </section>

              {/* Decision feed */}
              <section className="space-y-md">
                {/* Card 1 - Reviewed */}
                <article className="bg-df-surface-container-lowest p-md rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.05)] border border-df-outline-variant/50 relative hover:shadow-[0_8px_30px_rgba(0,0,0,0.08)] transition-all">
                  <div className="absolute top-md right-md bg-df-primary/10 text-df-primary px-sm py-xs rounded-full text-[11px] font-medium border border-df-primary/20 flex items-center gap-xs">
                    <Icon name="check_circle" className="text-[14px]" />
                    已复盘
                  </div>
                  <div className="pr-20">
                    <h3 className="text-[20px] font-semibold leading-7 text-df-on-surface mb-xs">是否接受 TechNova 的高级开发工程师 Offer？</h3>
                    <div className="flex gap-xs mb-md">
                      <span className="bg-df-surface-container-low text-df-on-surface-variant px-sm py-xs rounded-full text-[11px] font-medium border border-df-outline-variant/30">职业发展</span>
                      <span className="bg-df-secondary-container/20 text-df-on-secondary-container px-sm py-xs rounded-full text-[11px] font-medium border border-df-secondary/20">高优先级</span>
                    </div>
                  </div>
                  <div className="bg-df-surface-container-low p-sm rounded-lg mb-md">
                    <p className="text-[11px] font-medium text-df-on-surface-variant mb-xs flex items-center gap-xs">
                      <Icon name="info" className="text-[16px]" /> 背景信息
                    </p>
                    <p className="text-[14px] text-df-on-surface line-clamp-2">
                      目前公司处于平稳期，缺乏技术挑战。TechNova 提供 30% 涨薪及核心架构重构机会，但通勤时间增加 40 分钟，且试用期考核严格。
                    </p>
                  </div>
                  <div className="flex items-center justify-between border-t border-df-outline-variant/50 pt-md mt-sm">
                    <div className="flex items-center gap-sm">
                      <div className="w-8 h-8 rounded-full bg-df-primary-container text-df-primary flex items-center justify-center">
                        <Icon name="done" className="text-[18px]" />
                      </div>
                      <div>
                        <p className="text-[11px] font-medium text-df-on-surface-variant">最终选择</p>
                        <p className="text-[14px] font-medium text-df-on-surface">接受 Offer</p>
                      </div>
                    </div>
                    <span className="text-[11px] font-medium text-df-outline">2023-10-15</span>
                  </div>
                </article>

                {/* Card 2 - Pending */}
                <article className="bg-df-surface-container-lowest p-md rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.05)] border border-df-outline-variant/50 relative hover:shadow-[0_8px_30px_rgba(0,0,0,0.08)] transition-all">
                  <div className="absolute top-md right-md bg-df-tertiary-container/20 text-df-on-tertiary-container px-sm py-xs rounded-full text-[11px] font-medium border border-df-tertiary/20 flex items-center gap-xs">
                    <Icon name="schedule" className="text-[14px]" />
                    待回看
                  </div>
                  <div className="pr-20">
                    <h3 className="text-[20px] font-semibold leading-7 text-df-on-surface mb-xs">Q4 营销预算分配策略调整</h3>
                    <div className="flex gap-xs mb-md">
                      <span className="bg-df-surface-container-low text-df-on-surface-variant px-sm py-xs rounded-full text-[11px] font-medium border border-df-outline-variant/30">团队管理</span>
                      <span className="bg-df-surface-container-low text-df-on-surface-variant px-sm py-xs rounded-full text-[11px] font-medium border border-df-outline-variant/30">财务规划</span>
                    </div>
                  </div>
                  <div className="bg-df-surface-container-low p-sm rounded-lg mb-md">
                    <p className="text-[11px] font-medium text-df-on-surface-variant mb-xs flex items-center gap-xs">
                      <Icon name="info" className="text-[16px]" /> 背景信息
                    </p>
                    <p className="text-[14px] text-df-on-surface line-clamp-2">
                      双十一大促临近，原定于线下地推的 40% 预算效果预估不佳，考虑向线上社交媒体投放倾斜，需平衡各渠道代理商的利益诉求。
                    </p>
                  </div>
                  <div className="flex items-center justify-between border-t border-df-outline-variant/50 pt-md mt-sm">
                    <div className="flex items-center gap-sm opacity-60">
                      <div className="w-8 h-8 rounded-full bg-df-surface-variant text-df-outline flex items-center justify-center">
                        <Icon name="more_horiz" className="text-[18px]" />
                      </div>
                      <div>
                        <p className="text-[11px] font-medium text-df-on-surface-variant">最终选择</p>
                        <p className="text-[14px] italic text-df-outline">决策仍在执行中...</p>
                      </div>
                    </div>
                    <button className="text-df-primary text-[12px] font-semibold hover:underline">去回看</button>
                  </div>
                </article>
              </section>
            </div>
          </main>

          {/* Right sidebar */}
          <aside className="w-80 bg-df-surface-container-low border-l border-df-outline-variant overflow-y-auto hidden xl:block p-lg">
            <div className="mb-xl">
              <h2 className="text-[20px] font-semibold text-df-on-surface flex items-center gap-sm mb-md">
                <Icon name="edit_calendar" className="text-df-primary" />
                待回看任务
              </h2>
              <div className="space-y-sm">
                <div className="bg-df-surface-container-lowest p-sm rounded-lg shadow-sm border border-df-outline-variant/30 hover:border-df-primary/50 cursor-pointer transition-colors">
                  <div className="flex justify-between items-start mb-xs">
                    <span className="bg-df-error-container text-df-on-error-container px-2 py-0.5 rounded text-[10px] font-bold">逾期 2 天</span>
                    <Icon name="chevron_right" className="text-df-outline text-[16px]" />
                  </div>
                  <p className="text-[12px] font-semibold text-df-on-surface line-clamp-1">新产品线命名方案决议</p>
                </div>
                <div className="bg-df-surface-container-lowest p-sm rounded-lg shadow-sm border border-df-outline-variant/30 hover:border-df-primary/50 cursor-pointer transition-colors">
                  <div className="flex justify-between items-start mb-xs">
                    <span className="bg-df-tertiary-container/30 text-df-tertiary px-2 py-0.5 rounded text-[10px] font-bold">今天</span>
                  </div>
                  <p className="text-[12px] font-semibold text-df-on-surface line-clamp-1">年度健身计划执行方式</p>
                </div>
              </div>
            </div>

            <div>
              <h2 className="text-[20px] font-semibold text-df-on-surface flex items-center gap-sm mb-md">
                <Icon name="lightbulb" className="text-df-secondary" />
                本周洞察
              </h2>
              <div className="bg-df-primary-container/10 p-md rounded-xl border border-df-primary/20">
                <div className="flex items-center gap-md mb-sm">
                  <div className="w-12 h-12 rounded-full bg-df-surface-container-lowest shadow-sm flex items-center justify-center text-df-primary text-xl font-bold">
                    85<span className="text-xs">%</span>
                  </div>
                  <div>
                    <p className="text-[11px] font-medium text-df-on-surface-variant">决策满意度</p>
                    <p className="text-[12px] font-semibold text-df-on-surface">较上周上升 5%</p>
                  </div>
                </div>
                <p className="text-[14px] text-df-on-surface-variant mt-sm">
                  你最近在"职业发展"类的决策中表现出色，清晰地记录了背景信息，这有助于提高长期满意度。继续保持！
                </p>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}
