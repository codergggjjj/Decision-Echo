import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "提升编程能力 — Decisor AI" },
      { name: "description", content: "Long-term goal details with associated decisions, alignment and activity." },
    ],
  }),
  component: GoalDetailsPage,
});

type NavItem = { icon: string; label: string; active?: boolean };
const navItems: NavItem[] = [
  { icon: "dashboard", label: "仪表盘" },
  { icon: "gavel", label: "决策" },
  { icon: "bar_chart", label: "指标", active: true },
  { icon: "history", label: "历史" },
  { icon: "group", label: "团队" },
];

type Decision = {
  icon: string;
  title: string;
  date: string;
  status: "已达成" | "待处理" | "已审阅";
};
const decisions: Decision[] = [
  { icon: "school", title: "报名全栈开发在线课程", date: "2024年1月15日", status: "已达成" },
  { icon: "laptop_mac", title: "购买新MacBook Pro", date: "2024年3月2日", status: "待处理" },
  { icon: "library_books", title: "选择数据库技术栈 (SQL vs NoSQL)", date: "2024年2月20日", status: "已审阅" },
];

function StatusChip({ status }: { status: Decision["status"] }) {
  const map = {
    "已达成": {
      cls: "bg-surface-container text-on-surface-variant border-outline-variant",
      icon: "check_circle",
      iconCls: "text-tertiary",
    },
    "待处理": {
      cls: "bg-primary-fixed text-on-primary-fixed-variant border-primary-fixed-dim",
      icon: "pending",
      iconCls: "",
    },
    "已审阅": {
      cls: "bg-secondary-fixed text-on-secondary-fixed-variant border-secondary-fixed-dim",
      icon: "visibility",
      iconCls: "",
    },
  }[status];
  return (
    <span className={`px-3 py-1 rounded-full text-[14px] inline-flex items-center border ${map.cls}`}>
      <span className={`material-symbols-outlined text-[14px] mr-1 ${map.iconCls}`} style={{ fontSize: 14 }}>
        {map.icon}
      </span>
      {status}
    </span>
  );
}

function GoalDetailsPage() {
  return (
    <div className="flex min-h-screen text-on-background">
      {/* Side Nav */}
      <nav className="bg-surface-container-lowest shadow-md h-screen w-64 rounded-r-xl fixed left-0 top-0 flex-col p-md z-50 overflow-y-auto hidden md:flex">
        <div className="flex items-center gap-sm mb-lg">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-fixed to-secondary-fixed-dim shadow-sm flex items-center justify-center text-on-primary-fixed font-bold">
            D
          </div>
          <div>
            <h2 className="text-[20px] font-bold text-primary leading-tight">Decisor AI</h2>
            <p className="text-[12px] text-on-surface-variant">Enterprise Analytics</p>
          </div>
        </div>
        <div className="flex flex-col gap-xs flex-1">
          {navItems.map((item) => (
            <a
              key={item.label}
              href="#"
              className={
                item.active
                  ? "bg-secondary-container text-on-secondary-container rounded-full font-bold flex items-center px-md py-sm translate-x-1 transition-transform duration-200"
                  : "text-on-surface-variant flex items-center px-md py-sm hover:text-primary hover:bg-surface-container-high rounded-full transition-all duration-300"
              }
            >
              <span
                className="material-symbols-outlined mr-sm"
                style={{ fontVariationSettings: item.active ? "'FILL' 1" : "'FILL' 0" }}
              >
                {item.icon}
              </span>
              <span className="text-[14px]">{item.label}</span>
            </a>
          ))}
        </div>
      </nav>

      {/* Main */}
      <div className="flex-1 ml-0 md:ml-64 flex flex-col min-h-screen">
        {/* Top App Bar */}
        <header className="bg-surface-bright/80 backdrop-blur-md shadow-sm flex justify-between items-center w-full px-lg py-md z-40 sticky top-0">
          <div className="flex items-center gap-sm md:hidden">
            <button className="text-on-surface-variant" aria-label="Menu">
              <span className="material-symbols-outlined">menu</span>
            </button>
            <span className="text-[24px] font-bold text-primary">决策助手</span>
          </div>
          <div className="hidden md:flex flex-1 max-w-md relative">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant">
              search
            </span>
            <input
              type="text"
              placeholder="搜索..."
              className="w-full h-12 pl-10 pr-4 rounded-full border border-surface-variant bg-surface focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-colors text-[14px] text-on-surface"
            />
          </div>
          <div className="flex items-center gap-md">
            <button className="text-on-surface-variant hover:bg-surface-container transition-colors duration-200 p-2 rounded-full" aria-label="通知">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <button className="text-on-surface-variant hover:bg-surface-container transition-colors duration-200 p-2 rounded-full" aria-label="设置">
              <span className="material-symbols-outlined">settings</span>
            </button>
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-secondary-fixed to-primary-fixed border-2 border-surface-variant cursor-pointer" />
          </div>
        </header>

        <main className="flex-1 p-container-margin md:p-lg lg:p-xl max-w-7xl mx-auto w-full">
          {/* Page Header */}
          <div className="mb-lg flex flex-col md:flex-row md:items-start justify-between gap-md">
            <div>
              <a href="#" className="inline-flex items-center text-secondary hover:text-secondary-container transition-colors text-[14px] mb-sm">
                <span className="material-symbols-outlined mr-1" style={{ fontSize: 18 }}>arrow_back</span>
                返回长期目标列表
              </a>
              <div className="flex flex-wrap items-center gap-sm">
                <h1 className="text-[32px] font-bold tracking-tight text-on-surface m-0">提升编程能力</h1>
                <span className="bg-secondary-fixed text-on-secondary-fixed-variant px-3 py-1 rounded-full text-[14px] inline-flex items-center">
                  <span className="w-2 h-2 rounded-full bg-secondary mr-2" />
                  进行中
                </span>
              </div>
            </div>
            <button className="bg-primary hover:bg-primary-container text-on-primary text-[14px] py-3 px-6 rounded-full shadow-sm hover:shadow-md transition-all flex items-center justify-center gap-2 w-full md:w-auto">
              <span className="material-symbols-outlined" style={{ fontSize: 18 }}>add</span>
              为此目标创建决策
            </button>
          </div>

          {/* Bento Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-lg">
            {/* Left */}
            <div className="lg:col-span-2 flex flex-col gap-lg">
              {/* Goal Info */}
              <div className="glass-card rounded-xl p-lg">
                <h3 className="text-[18px] font-semibold text-on-surface mb-md">目标概览</h3>
                <p className="text-[16px] text-on-surface-variant mb-lg leading-relaxed">
                  掌握全栈开发技能，能够独立完成从前端到后端的完整Web应用开发。熟悉现代化框架如React和Node.js，并了解基本的DevOps流程。
                </p>
                <div className="bg-surface-container-low rounded-lg p-md mb-lg border border-surface-variant">
                  <div className="flex justify-between items-center mb-sm">
                    <span className="text-[14px] text-on-surface">整体进度</span>
                    <span className="text-[18px] font-semibold text-primary">40%</span>
                  </div>
                  <div className="w-full bg-surface-variant rounded-full h-3 overflow-hidden">
                    <div className="bg-primary h-3 rounded-full transition-all" style={{ width: "40%" }} />
                  </div>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-md">
                  <div className="flex items-start gap-sm">
                    <div className="bg-tertiary-fixed text-on-tertiary-fixed-variant p-2 rounded-lg">
                      <span className="material-symbols-outlined">event</span>
                    </div>
                    <div>
                      <span className="block text-[12px] text-on-surface-variant tracking-wider">截止日期</span>
                      <span className="text-[14px] text-on-surface font-semibold">2024年12月31日</span>
                    </div>
                  </div>
                  <div className="flex items-start gap-sm">
                    <div className="bg-secondary-fixed text-on-secondary-fixed-variant p-2 rounded-lg">
                      <span className="material-symbols-outlined">straighten</span>
                    </div>
                    <div>
                      <span className="block text-[12px] text-on-surface-variant tracking-wider">衡量标准</span>
                      <span className="text-[14px] text-on-surface font-semibold">完成3个全栈项目</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Associated Decisions */}
              <div>
                <h3 className="text-[18px] font-semibold text-on-surface mb-md px-1">相关决策</h3>
                <div className="flex flex-col gap-sm">
                  {decisions.map((d) => (
                    <div
                      key={d.title}
                      className="glass-card rounded-lg p-md flex items-center justify-between hover:bg-surface-container-lowest transition-colors cursor-pointer group"
                    >
                      <div className="flex items-center gap-md">
                        <div className="w-10 h-10 rounded-full bg-surface-container flex items-center justify-center text-secondary">
                          <span className="material-symbols-outlined">{d.icon}</span>
                        </div>
                        <div>
                          <h4 className="text-[16px] font-semibold text-on-surface group-hover:text-primary transition-colors">
                            {d.title}
                          </h4>
                          <p className="text-[12px] text-on-surface-variant">创建于：{d.date}</p>
                        </div>
                      </div>
                      <StatusChip status={d.status} />
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Right */}
            <div className="flex flex-col gap-lg">
              {/* Gauge */}
              <div className="glass-card rounded-xl p-lg flex flex-col items-center justify-center text-center">
                <h3 className="text-[18px] font-semibold text-on-surface mb-md w-full text-left">目标契合度</h3>
                <div
                  className="relative w-40 h-40 rounded-full flex items-center justify-center mb-md"
                  style={{ background: "conic-gradient(#9e3a68 0% 85%, #eceef0 85% 100%)" }}
                >
                  <div className="absolute inset-2 bg-surface rounded-full flex flex-col items-center justify-center shadow-inner">
                    <span className="text-[32px] font-bold text-primary leading-none">85%</span>
                    <span className="text-[12px] text-on-surface-variant mt-1">契合度</span>
                  </div>
                </div>
                <p className="text-[14px] text-on-surface-variant">
                  你的决策与此长期目标高度契合。
                </p>
              </div>

              {/* Stats */}
              <div className="glass-card rounded-xl p-lg">
                <h3 className="text-[18px] font-semibold text-on-surface mb-md">决策状态</h3>
                <div className="grid grid-cols-2 gap-sm">
                  <div className="bg-surface-container-low rounded-lg p-sm border border-surface-variant flex flex-col items-center justify-center py-4">
                    <span className="text-[20px] font-bold text-on-surface mb-1">12</span>
                    <span className="text-[12px] text-on-surface-variant">总计</span>
                  </div>
                  <div className="bg-surface-container-low rounded-lg p-sm border border-surface-variant flex flex-col items-center justify-center py-4">
                    <span className="text-[20px] font-bold text-secondary mb-1">8</span>
                    <span className="text-[12px] text-on-surface-variant">已审阅</span>
                  </div>
                  <div className="bg-primary-fixed rounded-lg p-sm border border-primary-fixed-dim flex flex-col items-center justify-center py-4">
                    <span className="text-[20px] font-bold text-on-primary-fixed-variant mb-1">4</span>
                    <span className="text-[12px] text-on-primary-fixed-variant">待处理</span>
                  </div>
                  <div className="bg-surface-container rounded-lg p-sm border border-outline-variant flex flex-col items-center justify-center py-4">
                    <span className="text-[20px] font-bold text-tertiary mb-1">7</span>
                    <span className="text-[12px] text-on-surface-variant">已达成</span>
                  </div>
                </div>
              </div>

              {/* Activity */}
              <div className="glass-card rounded-xl p-lg">
                <h3 className="text-[18px] font-semibold text-on-surface mb-md">最近动态</h3>
                <div className="relative pl-4 border-l-2 border-secondary-fixed">
                  <div className="mb-4 relative">
                    <div className="absolute -left-[21px] top-1 w-3 h-3 bg-secondary rounded-full border-2 border-surface shadow-[0_0_0_2px_rgba(0,96,172,0.2)]" />
                    <p className="text-[14px] text-on-surface">
                      状态变更为 <span className="font-semibold">进行中</span>
                    </p>
                    <p className="text-[12px] text-on-surface-variant">2天前</p>
                  </div>
                  <div className="mb-4 relative">
                    <div className="absolute -left-[21px] top-1 w-3 h-3 bg-surface-variant rounded-full border-2 border-surface" />
                    <p className="text-[14px] text-on-surface">新增决策「购买新MacBook Pro」</p>
                    <p className="text-[12px] text-on-surface-variant">1周前</p>
                  </div>
                  <div className="relative">
                    <div className="absolute -left-[21px] top-1 w-3 h-3 bg-surface-variant rounded-full border-2 border-surface" />
                    <p className="text-[14px] text-on-surface">目标已创建</p>
                    <p className="text-[12px] text-on-surface-variant">2024年1月10日</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
