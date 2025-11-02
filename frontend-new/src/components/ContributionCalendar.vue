<template>
  <div class="contribution-calendar">
    <div class="calendar-header">
      <div class="month-labels">
        <span v-for="month in monthLabels" :key="month.name" :style="{ gridColumn: month.column }">
          {{ month.name }}
        </span>
      </div>
    </div>
    
    <div class="calendar-grid">
      <!-- Day labels -->
      <div class="day-labels">
        <span>Mon</span>
        <span></span>
        <span>Wed</span>
        <span></span>
        <span>Fri</span>
        <span></span>
        <span></span>
      </div>
      
      <!-- Contribution cells -->
      <div class="weeks">
        <div v-for="(week, weekIndex) in weeks" :key="weekIndex" class="week">
          <div
            v-for="(day, dayIndex) in week"
            :key="dayIndex"
            :class="['day', getContributionLevel(day)]"
            :title="getDayTitle(day)"
            @click="onDayClick(day)"
          >
          </div>
        </div>
      </div>
    </div>
    
    <div class="calendar-footer">
      <span class="footer-text">Less</span>
      <div class="legend">
        <div class="legend-item level-0"></div>
        <div class="legend-item level-1"></div>
        <div class="legend-item level-2"></div>
        <div class="legend-item level-3"></div>
        <div class="legend-item level-4"></div>
      </div>
      <span class="footer-text">More</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
});

// 生成最近365天的日期网格
const weeks = computed(() => {
  const result = [];
  const today = new Date();
  const startDate = new Date(today);
  startDate.setDate(today.getDate() - 364); // 365天前
  
  // 找到起始日期所在周的周日
  const dayOfWeek = startDate.getDay();
  const firstSunday = new Date(startDate);
  firstSunday.setDate(startDate.getDate() - dayOfWeek);
  
  let currentDate = new Date(firstSunday);
  let currentWeek = [];
  
  // 生成53周的数据
  for (let i = 0; i < 53; i++) {
    currentWeek = [];
    for (let j = 0; j < 7; j++) {
      const dateStr = formatDate(currentDate);
      const count = props.data[dateStr] || 0;
      const isInRange = currentDate >= startDate && currentDate <= today;
      
      currentWeek.push({
        date: new Date(currentDate),
        dateStr: dateStr,
        count: isInRange ? count : null,
        isInRange: isInRange
      });
      
      currentDate.setDate(currentDate.getDate() + 1);
    }
    result.push(currentWeek);
  }
  
  return result;
});

// 月份标签
const monthLabels = computed(() => {
  const labels = [];
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  
  let lastMonth = -1;
  weeks.value.forEach((week, index) => {
    const firstDay = week[0];
    if (firstDay && firstDay.isInRange) {
      const month = firstDay.date.getMonth();
      if (month !== lastMonth && index > 0) {
        labels.push({
          name: months[month],
          column: index + 1
        });
        lastMonth = month;
      }
    }
  });
  
  return labels;
});

// 格式化日期为 YYYY-MM-DD
function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// 获取贡献等级
function getContributionLevel(day) {
  if (!day.isInRange || day.count === null) {
    return 'level-empty';
  }
  
  if (day.count === 0) return 'level-0';
  if (day.count <= 3) return 'level-1';
  if (day.count <= 6) return 'level-2';
  if (day.count <= 9) return 'level-3';
  return 'level-4';
}

// 获取日期提示
function getDayTitle(day) {
  if (!day.isInRange) return '';
  
  const dateStr = day.date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  
  if (day.count === 0) {
    return `${dateStr}: 无贡献`;
  }
  
  return `${dateStr}: ${day.count} 个贡献`;
}

// 点击日期
function onDayClick(day) {
  if (day.isInRange && day.count > 0) {
    console.log('Clicked day:', day);
  }
}
</script>

<style scoped>
.contribution-calendar {
  padding: 16px 0;
}

.calendar-header {
  margin-bottom: 4px;
}

.month-labels {
  display: grid;
  grid-template-columns: repeat(53, 11px);
  gap: 3px;
  padding-left: 30px;
  font-size: 10px;
  color: #666;
}

.calendar-grid {
  display: flex;
  gap: 8px;
}

.day-labels {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 9px;
  color: #666;
  padding-top: 0px;
  width: 22px;
}

.day-labels span {
  height: 11px;
  line-height: 11px;
  text-align: right;
}

.weeks {
  display: flex;
  gap: 3px;
}

.week {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.day {
  width: 11px;
  height: 11px;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.day:hover {
  outline: 2px solid rgba(0, 0, 0, 0.3);
  outline-offset: 0px;
}

/* GitHub 风格的颜色 */
.level-empty {
  background-color: transparent;
  cursor: default;
}

.level-empty:hover {
  outline: none;
}

.level-0 {
  background-color: #ebedf0;
}

.level-1 {
  background-color: #9be9a8;
}

.level-2 {
  background-color: #40c463;
}

.level-3 {
  background-color: #30a14e;
}

.level-4 {
  background-color: #216e39;
}

.calendar-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 8px;
  font-size: 11px;
  color: #666;
}

.footer-text {
  font-size: 11px;
}

.legend {
  display: flex;
  gap: 3px;
}

.legend-item {
  width: 11px;
  height: 11px;
  border-radius: 2px;
}

.legend-item.level-0 {
  background-color: #ebedf0;
}

.legend-item.level-1 {
  background-color: #9be9a8;
}

.legend-item.level-2 {
  background-color: #40c463;
}

.legend-item.level-3 {
  background-color: #30a14e;
}

.legend-item.level-4 {
  background-color: #216e39;
}
</style>

