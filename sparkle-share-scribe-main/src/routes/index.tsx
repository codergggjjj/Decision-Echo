import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "决策回声 Dashboard" },
      { name: "description", content: "记录、回看与复盘你的每一个决定。" },
      { property: "og:title", content: "决策回声 Dashboard" },
      { property: "og:description", content: "记录、回看与复盘你的每一个决定。" },
    ],
  }),
  component: Index,
});

const PINK = "#FB7299";
const BLUE = "#00A1D6";

function Icon({ name, className = "", style }: { name: string; className?: string; style?: React.CSSProperties }) {
  return <span className={`material-symbols-outlined ${className}`} style={style}>{name}</span>;
}

function Index() {
  return (
    <div className="min-h-screen bg-[#F4F5F7] text-[#191c1e]">
      {/* Top Nav */}
      <nav className="bg-white border-b border-[#e0e3e5] fixed top-0 w-full z-50 flex justify-between items-center h-16 px-6 soft-shadow">
        <div className="flex items-center space-x-4">
          <span className="text-2xl font-black tracking-tight text-[#191c1e]">决策回声</span>
        </div>
        <div className="flex items-center space-x-3 text-[#3c4a46]">
          <Icon name="notifications" className="cursor-pointer hover:text-[#FB7299] transition-colors" />
          <Icon name="settings" className="cursor-pointer hover:text-[#FB7299] transition-colors" />
          <img
            alt="User profile"
            className="w-8 h-8 rounded-full border border-[#e0e3e5] cursor-pointer active:opacity-80 transition-opacity object-cover"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuBMptVclut6rsVFgDrVwfx4AADL86RZwVZmoobA5BxFvPEh1xeERSuAeuDuzKJwMRMeMEVprPNy9i_4Oq4CvvXlFVpsvYQtOz7aRcahw7CSqWm6F0eZ2YtilPPuI_2vkJ8DzxAczibS1O6WdkPuS_X5eZytRBd-bp9ImjUFSDGhTmceJ40nIxAU6thH7-1_yedcMaZNVqLHlkkJpqtdWp9jF97OC2UepNe6LvIkk20F0sfkyHmNzCWtSKDTB5JIsmN9K_SM5jHGIAed"
          />
        </div>
      </nav>

      {/* Sidebar */}
      <aside className="bg-white fixed left-0 top-16 h-[calc(100vh-64px)] z-40 border-r border-[#e0e3e5] hidden md:flex flex-col p-4 space-y-3 w-56">
        <div className="mb-6 px-2">
          <h2 className="text-xl font-bold text-[#191c1e]">决策回声</h2>
          <p className="text-sm text-[#3c4a46]">管理平台</p>
        </div>
        <nav className="flex-1 space-y-1">
          {[
            { icon: "home", label: "首页", active: true },
            { icon: "history", label: "历史" },
            { icon: "analytics", label: "统计" },
            { icon: "person", label: "我的" },
          ].map((item) => (
            <a
              key={item.label}
              href="#"
              className={`rounded-lg flex items-center px-3 py-3 transition-all cursor-pointer ${
                item.active
                  ? "bg-[#FB7299]/10 text-[#FB7299] font-bold"
                  : "text-[#3c4a46] hover:bg-[#e6e8ea] hover:text-[#00A1D6]"
              }`}
            >
              <Icon name={item.icon} className={`mr-3 ${item.active ? "icon-fill" : ""}`} />
              {item.label}
            </a>
          ))}
        </nav>
        <button className="bg-[#FB7299] text-white rounded-full py-3 px-4 font-bold text-sm flex items-center justify-center hover:opacity-90 transition-opacity soft-shadow mt-auto">
          <Icon name="add" className="mr-2 text-base" />
          记录新决定
        </button>
      </aside>

      {/* Main */}
      <main className="pt-16 min-h-screen md:pl-56">
        <div className="max-w-[1440px] mx-auto p-6 md:p-8 flex flex-col lg:flex-row gap-8">
          {/* Left */}
          <div className="lg:w-[65%] flex flex-col">
            {/* Stats */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <StatCard color={BLUE} bg="from-white to-blue-50" border="border-blue-100" badge="全部" icon="receipt_long" label="全部记录" value="27" glow="hover:shadow-glow-blue" />
              <StatCard color="#FFB000" bg="from-white to-yellow-50" border="border-yellow-100" badge="待办" icon="schedule" label="待回看" value="9" />
              <StatCard color={PINK} bg="from-white to-pink-50" border="border-pink-100" badge="完成" icon="task_alt" label="已复盘" value="18" glow="hover:shadow-glow-pink" />
              <StatCard color={BLUE} bg="from-white to-blue-50" border="border-blue-100" badge="指标" icon="sentiment_satisfied" label="满意率" value="67%" glow="hover:shadow-glow-blue" />
            </div>

            {/* Search */}
            <div className="bg-white rounded-2xl p-3 mb-6 flex flex-col sm:flex-row gap-3 items-center soft-shadow">
              <div className="relative flex-1 w-full">
                <input
                  className="w-full bg-[#f2f4f6] border-none rounded-xl py-3 pl-4 pr-10 focus:bg-white focus:ring-2 focus:ring-[#00A1D6] focus:outline-none transition-colors text-sm text-[#191c1e]"
                  placeholder="搜决策名，比如 周末课程"
                  type="text"
                />
                <Icon name="search" className="absolute right-3 top-1/2 -translate-y-1/2 text-[#3c4a46]" />
              </div>
              <div className="flex gap-2 w-full sm:w-auto overflow-x-auto pb-2 sm:pb-0 hide-scrollbar">
                <button className="bg-[#f2f4f6] text-[#191c1e] font-semibold text-xs px-4 py-3 rounded-xl whitespace-nowrap hover:bg-[#e6e8ea] transition-colors flex items-center">
                  全部标签 <Icon name="expand_more" className="text-sm align-middle ml-1" />
                </button>
                <button className="bg-[#FB7299] text-white font-semibold text-xs px-4 py-3 rounded-xl whitespace-nowrap hover:opacity-90 transition-colors shadow-[0_4px_10px_rgba(251,114,153,0.3)]">查询</button>
                <button className="bg-[#e6e8ea] text-[#3c4a46] font-semibold text-xs px-4 py-3 rounded-xl whitespace-nowrap hover:bg-[#d8dadc] transition-colors">清空</button>
              </div>
            </div>

            {/* Filter pills */}
            <div className="flex gap-3 mb-6">
              <button className="text-xs px-4 py-1.5 rounded-full bg-[#FB7299] text-white font-bold shadow-md shadow-[#FB7299]/30">全部</button>
              <button className="text-xs px-4 py-1.5 rounded-full bg-white text-[#3c4a46] hover:bg-[#f2f4f6] transition-colors shadow-sm">待回看</button>
              <button className="text-xs px-4 py-1.5 rounded-full bg-white text-[#3c4a46] hover:bg-[#f2f4f6] transition-colors shadow-sm">已复盘</button>
            </div>

            {/* Timeline */}
            <div className="relative border-l-2 border-[#00A1D6]/30 space-y-8 pl-8 ml-16">
              <TimelineItem
                date={["2026", "05-08"]}
                nodeColor={BLUE}
                gradient="from-white to-[#f0f8fb]"
                border="border-[#d6eff8]"
                hoverShadow="hover:shadow-glow-blue"
                bgIcon="task_alt"
                bgIconColor="text-[#00A1D6]/5"
                status={{ label: "已复盘", icon: "check_circle", color: BLUE }}
                tags={["工作", "时间管理"]}
                title="演示-是否拒绝一个临时兼职"
                finalChoice={{ label: "拒绝", icon: "close", color: PINK }}
                options={[
                  { text: "拒绝", selected: true, color: PINK },
                  { text: "接受" },
                  { text: "协商减少时长" },
                ]}
                footer={
                  <button className="text-[#00A1D6] bg-white hover:bg-[#00A1D6]/10 px-4 py-1.5 rounded-full text-xs font-bold transition-colors shadow-sm border border-[#00A1D6]/20">
                    查看详情
                  </button>
                }
              />
              <TimelineItem
                date={["2026-", "05-31"]}
                nodeColor={PINK}
                gradient="from-white to-[#fff0f4]"
                border="border-[#ffe0e8]"
                hoverShadow="hover:shadow-glow-pink"
                bgIcon="schedule"
                bgIconColor="text-[#FB7299]/5"
                status={{ label: "待回看", icon: "pending_actions", color: PINK }}
                tags={["健康", "生活"]}
                title="演示-是否减少咖啡摄入"
                finalChoice={{ label: "只上午喝", icon: "done", color: BLUE, textColor: "#191c1e" }}
                options={[
                  { text: "只上午喝", selected: true, color: BLUE },
                  { text: "换成茶" },
                  { text: "继续原习惯" },
                ]}
                topRight={
                  <button className="bg-[#FB7299] text-white text-xs px-4 py-1.5 rounded-full font-bold shadow-[0_4px_10px_rgba(251,114,153,0.3)] hover:opacity-90 transition-colors flex items-center gap-1">
                    <Icon name="replay" className="text-sm" />
                    现在回看
                  </button>
                }
              />
            </div>
          </div>

          {/* Right */}
          <div className="lg:w-[35%] flex flex-col space-y-6">
            {/* To Review */}
            <section className="bg-gradient-to-br from-white to-[#fff5f8] rounded-2xl p-6 border border-pink-100/50 shadow-[0_8px_30px_rgb(251,114,153,0.06),0_4px_10px_rgb(0,0,0,0.02)] relative overflow-hidden">
              <div className="absolute -top-10 -right-10 w-32 h-32 bg-[#FB7299]/5 rounded-full blur-3xl" />
              <div className="flex justify-between items-center mb-6 relative z-10">
                <h3 className="text-lg font-bold text-[#191c1e] flex items-center gap-2">
                  <Icon name="pending_actions" className="text-[#FB7299]" />
                  待回看
                </h3>
                <span className="bg-[#FB7299] text-white font-bold px-3 py-1 rounded-full text-sm shadow-[0_4px_12px_rgba(251,114,153,0.4)] flex items-center justify-center min-w-[28px]">
                  2
                </span>
              </div>
              <div className="space-y-4 relative z-10">
                <ReviewCard time="2026-05-25 13:26" title="是否报名周末课程" />
                <ReviewCard time="2026-05-27 17:50" title="演示-是否参加周末线下活动" />
              </div>
            </section>

            {/* Results */}
            <section className="bg-gradient-to-br from-white to-[#f8faff] rounded-2xl p-6 border border-blue-100/40 soft-shadow relative overflow-hidden">
              <div className="absolute -top-12 -right-12 w-40 h-40 bg-[#00A1D6]/5 rounded-full blur-3xl" />
              <div className="mb-6 relative z-10">
                <h3 className="text-xs text-[#3c4a46]/70 mb-1 tracking-wider uppercase font-semibold">复盘心情</h3>
                <h2 className="text-xl font-bold text-[#191c1e]">回测结果统计</h2>
              </div>
              <div className="space-y-4 relative z-10">
                <StatBar
                  icon="sentiment_very_satisfied"
                  label="满意"
                  iconColor="text-[#00A1D6]"
                  valueColor="text-[#00A1D6]"
                  value={12}
                  width="66%"
                  barGradient="from-[#00A1D6] to-[#62fae3]"
                  hover="hover:shadow-glow-blue"
                />
                <StatBar
                  icon="sentiment_neutral"
                  label="一般"
                  iconColor="text-[#FFB000]"
                  valueColor="text-[#3c4a46]/80"
                  value={3}
                  width="16%"
                  barGradient="from-[#FFB000] to-[#ffdcc0]"
                  hover="hover:shadow-md"
                />
                <StatBar
                  icon="sentiment_very_dissatisfied"
                  label="后悔"
                  iconColor="text-[#FB7299]"
                  valueColor="text-[#FB7299]"
                  value={3}
                  width="16%"
                  barGradient="from-[#FB7299] to-[#ffafd3]"
                  hover="hover:shadow-glow-pink"
                />
              </div>
            </section>
          </div>
        </div>
      </main>
    </div>
  );
}

function StatCard({
  color,
  bg,
  border,
  badge,
  icon,
  label,
  value,
  glow = "",
}: {
  color: string;
  bg: string;
  border: string;
  badge: string;
  icon: string;
  label: string;
  value: string;
  glow?: string;
}) {
  return (
    <div
      className={`bg-gradient-to-br ${bg} border ${border} rounded-2xl p-4 shadow-sm flex flex-col justify-center items-center text-center relative overflow-hidden hover:-translate-y-1 ${glow} transition-all duration-300`}
    >
      <div
        className="absolute top-3 right-3 text-[10px] font-bold px-2 py-1 rounded-full uppercase tracking-wider"
        style={{ backgroundColor: `${color}1A`, color }}
      >
        {badge}
      </div>
      <Icon name={icon} className="mb-2 text-3xl opacity-80" />
      <style>{``}</style>
      <h3 className="text-base font-bold text-[#191c1e] relative z-10">{label}</h3>
      <p className="text-3xl font-black mt-1 relative z-10 tracking-tight" style={{ color }}>
        {value}
      </p>
      <style>{`
        .stat-icon-${badge} { color: ${color}; }
      `}</style>
    </div>
  );
}

type Opt = { text: string; selected?: boolean; color?: string };

function TimelineItem({
  date,
  nodeColor,
  gradient,
  border,
  hoverShadow,
  bgIcon,
  bgIconColor,
  status,
  tags,
  title,
  finalChoice,
  options,
  footer,
  topRight,
}: {
  date: [string, string];
  nodeColor: string;
  gradient: string;
  border: string;
  hoverShadow: string;
  bgIcon: string;
  bgIconColor: string;
  status: { label: string; icon: string; color: string };
  tags: string[];
  title: string;
  finalChoice: { label: string; icon: string; color: string; textColor?: string };
  options: Opt[];
  footer?: React.ReactNode;
  topRight?: React.ReactNode;
}) {
  return (
    <div className="relative">
      <div
        className="absolute -left-[34px] top-6 w-3 h-3 rounded-full border-2 border-white shadow-sm z-10"
        style={{ backgroundColor: nodeColor }}
      />
      <div className="absolute -left-[78px] top-5 text-xs font-bold text-[#3c4a46] text-right w-10 leading-tight">
        {date[0]}
        <div>{date[1]}</div>
      </div>
      <div
        className={`bg-gradient-to-br ${gradient} border ${border} rounded-2xl p-4 shadow-sm ${hoverShadow} transition-all duration-300 w-full group relative overflow-hidden`}
      >
        <Icon name={bgIcon} className={`absolute -bottom-4 -right-4 text-9xl ${bgIconColor} pointer-events-none transform -rotate-12`} />
        <div className="flex justify-between items-start mb-3 relative z-10">
          <div className="flex items-center gap-3 flex-wrap">
            <span
              className="px-2.5 py-1 rounded-md text-[11px] font-bold flex items-center gap-1 shadow-sm"
              style={{ backgroundColor: `${status.color}26`, color: status.color }}
            >
              <Icon name={status.icon} className="text-sm" />
              {status.label}
            </span>
            <div className="flex gap-2">
              {tags.map((t) => (
                <span key={t} className="bg-white text-[#3c4a46] px-2 py-0.5 rounded-full text-xs border border-[#e0e3e5]/40 shadow-sm">
                  {t}
                </span>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-2">
            <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity bg-white/80 backdrop-blur-sm rounded-full px-2 py-1 shadow-sm">
              <button className="text-[#3c4a46] hover:text-[#00A1D6]">
                <Icon name="visibility" className="text-lg" />
              </button>
              <button className="text-[#3c4a46] hover:text-[#FB7299]">
                <Icon name="delete" className="text-lg" />
              </button>
            </div>
            {topRight}
          </div>
        </div>
        <h4 className="text-xl font-bold text-[#191c1e] mb-2 relative z-10">{title}</h4>
        <div className="mb-3 flex items-center relative z-10">
          <span className="text-[#3c4a46] text-xs mr-3 bg-white/60 px-2 py-1 rounded-md border border-[#e0e3e5]/20 shadow-sm">
            最终选择
          </span>
          <span
            className="font-bold text-xs flex items-center gap-1"
            style={{ color: finalChoice.textColor ?? finalChoice.color }}
          >
            <Icon name={finalChoice.icon} className="text-base" style={{ color: finalChoice.color }} />
            {finalChoice.label}
          </span>
        </div>
        <div className="flex flex-wrap gap-2 relative z-10">
          {options.map((o) =>
            o.selected ? (
              <span
                key={o.text}
                className="px-3 py-1.5 rounded-full text-xs shadow-sm flex items-center"
                style={{
                  backgroundColor: `${o.color}1A`,
                  color: o.color,
                  borderWidth: 1,
                  borderColor: `${o.color}33`,
                }}
              >
                {o.text}
              </span>
            ) : (
              <span key={o.text} className="bg-white text-[#191c1e] px-3 py-1.5 rounded-full text-xs shadow-sm border border-[#e0e3e5]/20">
                {o.text}
              </span>
            )
          )}
        </div>
        {footer && <div className="mt-4 flex justify-end gap-3 relative z-10">{footer}</div>}
      </div>
    </div>
  );
}

function ReviewCard({ time, title }: { time: string; title: string }) {
  return (
    <div className="bg-white/80 backdrop-blur-sm rounded-xl p-4 hover:-translate-y-1 transition-all duration-300 border border-white shadow-sm hover:shadow-glow-pink group cursor-pointer">
      <div className="flex justify-between items-start mb-3">
        <p className="text-[#3c4a46] text-[11px] font-medium flex items-center gap-1.5">
          <Icon name="schedule" className="text-sm opacity-70" />
          {time}
        </p>
        <Icon name="edit_note" className="text-[#FB7299]/20 group-hover:text-[#FB7299] transition-colors" />
      </div>
      <h4 className="text-base font-bold text-[#191c1e] mb-4 leading-snug">{title}</h4>
      <div className="flex justify-end">
        <button className="bg-[#FB7299] text-white text-xs px-5 py-2 rounded-full font-bold shadow-md hover:shadow-glow-pink hover:opacity-90 transition-all active:scale-95">
          补回测
        </button>
      </div>
    </div>
  );
}

function StatBar({
  icon,
  label,
  iconColor,
  valueColor,
  value,
  width,
  barGradient,
  hover,
}: {
  icon: string;
  label: string;
  iconColor: string;
  valueColor: string;
  value: number;
  width: string;
  barGradient: string;
  hover: string;
}) {
  return (
    <div className={`bg-white/60 backdrop-blur-sm p-4 rounded-2xl shadow-sm border border-white ${hover} transition-all duration-300 group`}>
      <div className="flex justify-between items-center mb-3">
        <span className="text-[#191c1e] font-bold flex items-center gap-2">
          <Icon name={icon} className={`text-xl ${iconColor} icon-fill`} />
          {label}
        </span>
        <span className={`text-2xl font-black ${valueColor} tracking-tighter`}>{value}</span>
      </div>
      <div className="w-full bg-[#f2f4f6]/50 rounded-full h-2.5 overflow-hidden shadow-inner">
        <div
          className={`bg-gradient-to-r ${barGradient} h-full rounded-full transition-all duration-1000`}
          style={{ width }}
        />
      </div>
    </div>
  );
}
