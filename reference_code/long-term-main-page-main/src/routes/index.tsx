import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "长期目标 - Decisor AI" },
      { name: "description", content: "让每一次决策都更靠近长期方向" },
    ],
  }),
  component: LongTermGoals,
});

type Goal = {
  title: string;
  description: string;
  priority: "高优先级" | "中优先级" | "低优先级";
  category: string;
  categoryColor: string;
  progress: number;
  daysLeft: number;
  deadline: string;
  decisions: number;
  progressColor: string;
};

const goals: Goal[] = [
  {
    title: "提升编程能力",
    description: "掌握全栈开发技能，完成3个独立项目并上线，每天保证2小时学习时间。",
    priority: "高优先级",
    category: "学习",
    categoryColor: "bg-secondary-fixed text-on-secondary-fixed",
    progress: 40,
    daysLeft: 180,
    deadline: "2024-12-31",
    decisions: 8,
    progressColor: "bg-primary",
  },
  {
    title: "马拉松完赛",
    description: "完成人生首个半程马拉松，配速控制在6分30秒以内，每周保持3次有氧训练。",
    priority: "中优先级",
    category: "健康",
    categoryColor: "bg-tertiary-fixed text-on-tertiary-fixed",
    progress: 75,
    daysLeft: 45,
    deadline: "2024-05-20",
    decisions: 3,
    progressColor: "bg-secondary",
  },
  {
    title: "建立应急基金",
    description: "存满6个月的日常开销作为应急基金，优化每月支出结构，减少不必要消费。",
    priority: "高优先级",
    category: "财务",
    categoryColor: "bg-surface-variant text-on-surface-variant",
    progress: 15,
    daysLeft: 300,
    deadline: "2024-10-01",
    decisions: 12,
    progressColor: "bg-tertiary",
  },
];

const navItems = [
  { icon: "dashboard", label: "Dashboard" },
  { icon: "gavel", label: "Decisions" },
  { icon: "bar_chart", label: "Metrics" },
  { icon: "flag", label: "长期目标", active: true },
  { icon: "history", label: "History" },
  { icon: "group", label: "Team" },
];

const stats = [
  { icon: "trending_up", label: "进行中目标", value: "12", iconBg: "bg-primary-fixed text-on-primary-fixed" },
  { icon: "check_circle", label: "已完成目标", value: "45", iconBg: "bg-secondary-fixed text-on-secondary-fixed" },
  { icon: "cancel", label: "已放弃目标", value: "3", iconBg: "bg-surface-container-high text-on-surface-variant" },
  { icon: "account_tree", label: "关联决策总数", value: "128", iconBg: "bg-tertiary-fixed text-on-tertiary-fixed" },
];

function LongTermGoals() {
  return (
    <div className="font-body-md text-on-surface min-h-screen flex">
      {/* Side Nav */}
      <nav className="hidden md:flex flex-col bg-surface-container-lowest shadow-md h-screen w-64 rounded-r-xl fixed left-0 top-0 p-md z-50 overflow-y-auto">
        <div className="flex items-center gap-sm mb-xl">
          <div className="w-10 h-10 rounded-lg bg-primary-container flex items-center justify-center text-on-primary-container">
            <span className="material-symbols-outlined">psychology</span>
          </div>
          <div>
            <h1 className="font-headline-md text-headline-md text-primary">Decisor AI</h1>
            <p className="font-label-md text-label-md text-on-surface-variant">Enterprise Analytics</p>
          </div>
        </div>
        <div className="flex flex-col gap-sm flex-grow">
          {navItems.map((item) => (
            <a
              key={item.label}
              href="#"
              className={
                item.active
                  ? "bg-secondary-container text-on-secondary-container rounded-full font-bold flex items-center gap-sm px-md py-sm translate-x-1 transition-transform duration-200"
                  : "text-on-surface-variant flex items-center gap-sm px-md py-sm hover:text-primary hover:bg-surface-container-high rounded-full transition-all duration-300"
              }
            >
              <span className="material-symbols-outlined">{item.icon}</span>
              <span className="font-label-md text-label-md">{item.label}</span>
            </a>
          ))}
        </div>
      </nav>

      {/* Main */}
      <main className="flex-1 md:ml-64 flex flex-col min-h-screen">
        {/* Top Bar */}
        <header className="bg-surface-bright shadow-sm flex justify-between items-center w-full px-lg py-md z-40 sticky top-0">
          <div className="flex-1 max-w-xl">
            <div className="relative flex items-center w-full h-12 rounded-full bg-surface-container focus-within:ring-2 focus-within:ring-primary transition-all">
              <span className="material-symbols-outlined absolute left-4 text-on-surface-variant">search</span>
              <input
                className="w-full h-full bg-transparent border-none focus:ring-0 focus:outline-none pl-12 pr-4 font-body-md text-body-md text-on-surface placeholder:text-on-surface-variant rounded-full"
                placeholder="Search goals, decisions, tags..."
                type="text"
              />
            </div>
          </div>
          <div className="flex items-center gap-md ml-auto">
            <button className="w-10 h-10 rounded-full hover:bg-surface-container flex items-center justify-center text-on-surface-variant transition-colors">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <button className="w-10 h-10 rounded-full hover:bg-surface-container flex items-center justify-center text-on-surface-variant transition-colors">
              <span className="material-symbols-outlined">settings</span>
            </button>
            <div className="w-10 h-10 rounded-full border border-outline-variant bg-primary-fixed flex items-center justify-center text-on-primary-fixed font-bold">
              U
            </div>
          </div>
        </header>

        {/* Content */}
        <div className="p-lg max-w-7xl mx-auto w-full flex flex-col gap-lg">
          {/* Header */}
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-md">
            <div>
              <h2 className="font-display-lg text-display-lg text-on-surface">长期目标</h2>
              <p className="font-body-lg text-body-lg text-on-surface-variant mt-xs">
                让每一次决策都更靠近长期方向
              </p>
            </div>
            <button className="bg-primary hover:bg-on-primary-container text-on-primary font-label-md text-label-md px-lg py-sm h-12 rounded-full flex items-center gap-xs shadow-sm transition-all transform hover:-translate-y-1">
              <span className="material-symbols-outlined">add</span>
              新建目标
            </button>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-md">
            {stats.map((s) => (
              <div
                key={s.label}
                className="bg-surface-container-lowest rounded-xl p-md card-shadow border border-surface-container flex items-center gap-md"
              >
                <div className={`w-12 h-12 rounded-full flex items-center justify-center ${s.iconBg}`}>
                  <span className="material-symbols-outlined">{s.icon}</span>
                </div>
                <div>
                  <p className="font-label-sm text-label-sm text-on-surface-variant uppercase tracking-wider">
                    {s.label}
                  </p>
                  <p className="font-headline-md text-headline-md text-on-surface mt-xs">{s.value}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Filters */}
          <div className="flex flex-col sm:flex-row justify-between items-center gap-md bg-surface-container-lowest p-sm rounded-xl card-shadow border border-surface-container">
            <div className="flex gap-sm overflow-x-auto w-full sm:w-auto no-scrollbar">
              {["全部", "进行中", "已完成", "已放弃"].map((f, i) => (
                <button
                  key={f}
                  className={
                    i === 0
                      ? "bg-primary text-on-primary font-label-md text-label-md px-md py-sm rounded-full whitespace-nowrap"
                      : "bg-surface-container hover:bg-surface-container-high text-on-surface-variant font-label-md text-label-md px-md py-sm rounded-full whitespace-nowrap transition-colors"
                  }
                >
                  {f}
                </button>
              ))}
            </div>
            <div className="flex gap-sm w-full sm:w-auto">
              {["分类", "优先级"].map((f) => (
                <button
                  key={f}
                  className="flex items-center gap-xs px-md py-sm border border-outline-variant rounded-full text-on-surface-variant font-label-md text-label-md hover:bg-surface-container transition-colors"
                >
                  {f}
                  <span className="material-symbols-outlined" style={{ fontSize: 16 }}>
                    arrow_drop_down
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* Goals Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
            {goals.map((g) => (
              <div
                key={g.title}
                className="goal-card bg-surface-container-lowest rounded-xl p-md card-shadow border border-surface-container flex flex-col gap-sm relative overflow-hidden group transition-all hover:-translate-y-1"
              >
                <div className="flex justify-between items-start">
                  <div className="flex gap-xs">
                    <span
                      className={
                        g.priority === "高优先级"
                          ? "bg-primary-fixed text-on-primary-fixed font-label-sm text-label-sm px-sm py-xs rounded-full"
                          : "bg-surface-container-high text-on-surface font-label-sm text-label-sm px-sm py-xs rounded-full"
                      }
                    >
                      {g.priority}
                    </span>
                    <span className={`${g.categoryColor} font-label-sm text-label-sm px-sm py-xs rounded-full`}>
                      {g.category}
                    </span>
                  </div>
                  <div className="goal-actions opacity-0 transition-opacity flex gap-xs bg-surface-container-lowest shadow-sm rounded-full p-xs">
                    <button
                      className="w-8 h-8 rounded-full hover:bg-surface-container flex items-center justify-center text-on-surface-variant"
                      title="编辑"
                    >
                      <span className="material-symbols-outlined" style={{ fontSize: 18 }}>
                        edit
                      </span>
                    </button>
                    <button
                      className="w-8 h-8 rounded-full hover:bg-error-container hover:text-error flex items-center justify-center text-on-surface-variant"
                      title="删除"
                    >
                      <span className="material-symbols-outlined" style={{ fontSize: 18 }}>
                        delete
                      </span>
                    </button>
                  </div>
                </div>
                <h3 className="font-headline-sm text-headline-sm text-on-surface mt-xs">{g.title}</h3>
                <p className="font-body-md text-body-md text-on-surface-variant line-clamp-2">{g.description}</p>
                <div className="mt-auto pt-sm flex flex-col gap-xs">
                  <div className="flex justify-between items-center">
                    <span className="font-label-sm text-label-sm text-on-surface-variant">
                      进度 ({g.progress}%)
                    </span>
                    <span className="font-label-sm text-label-sm text-on-surface-variant">
                      剩余 {g.daysLeft} 天
                    </span>
                  </div>
                  <div className="w-full h-2 bg-surface-container-high rounded-full overflow-hidden">
                    <div
                      className={`h-full ${g.progressColor} rounded-full`}
                      style={{ width: `${g.progress}%` }}
                    />
                  </div>
                  <div className="flex justify-between items-center mt-sm border-t border-surface-variant pt-sm">
                    <div className="flex items-center gap-xs text-secondary">
                      <span className="material-symbols-outlined" style={{ fontSize: 16 }}>
                        link
                      </span>
                      <span className="font-label-sm text-label-sm">{g.decisions}个决策</span>
                    </div>
                    <span className="font-label-sm text-label-sm text-on-surface-variant">
                      {g.deadline} 截止
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}
