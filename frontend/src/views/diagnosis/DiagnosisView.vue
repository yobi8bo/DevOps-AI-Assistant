<template>
  <div class="page diagnosis-page">
    <div class="page-header">
      <div>
        <h1>智能排障</h1>
        <p>输入故障描述、日志和命令输出，生成结构化排查建议。</p>
      </div>
      <a-button type="primary" :loading="loading" @click="submitAnalyze">
        <thunderbolt-outlined />
        开始分析
      </a-button>
    </div>

    <a-row :gutter="[16, 16]" class="diagnosis-workbench">
      <a-col :xs="24" :xl="11">
        <a-card title="故障输入" :bordered="false" class="diagnosis-input-card">
          <a-form layout="vertical">
            <a-form-item label="故障标题">
              <a-input v-model:value="form.title" placeholder="例如：Docker 执行 docker ps 权限不足" />
            </a-form-item>
            <a-form-item label="故障类型">
              <a-select v-model:value="form.category" :options="categoryOptions" />
            </a-form-item>
            <a-form-item label="运行环境">
              <a-input v-model:value="form.environment" placeholder="例如：Ubuntu 24.04 + Docker" />
            </a-form-item>
            <a-row :gutter="12">
              <a-col :span="12">
                <a-form-item label="紧急程度">
                  <a-select v-model:value="form.urgencyLevel" :options="urgencyOptions" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="生产环境">
                  <a-switch v-model:checked="form.isProduction" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="故障描述">
              <a-textarea v-model:value="form.description" :rows="4" placeholder="描述故障现象、最近变更和影响范围" />
            </a-form-item>
            <a-form-item label="日志或命令输出">
              <a-textarea v-model:value="form.commandOutput" :rows="10" placeholder="粘贴报错日志、命令输出或截图 OCR 文字" />
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="13">
        <a-card title="AI 分析结果" :bordered="false" class="diagnosis-result-card">
          <div v-if="!result" class="result-empty">
            <a-empty description="提交后将在这里显示结构化排障建议" />
          </div>
          <template v-else>
            <div class="result-scroll">
              <div class="result">
                <a-alert
                  v-if="result.riskLevel !== 'LOW'"
                  :type="riskAlertType"
                  show-icon
                  :message="riskText"
                  :description="riskDescription"
                />
                <a-alert
                  v-if="form.isProduction && result.riskLevel !== 'LOW'"
                  class="risk-alert"
                  type="error"
                  show-icon
                  message="生产环境风险提示"
                  description="当前排障目标标记为生产环境。执行任何 HIGH 或 CRITICAL 命令前，应完成备份确认、影响评估和回滚方案确认。"
                />
                <section>
                  <h2>问题摘要</h2>
                  <p>{{ result.summary }}</p>
                </section>
                <section>
                  <h2>可能原因</h2>
                  <a-list size="small" :data-source="result.possibleCauses">
                    <template #renderItem="{ item }">
                      <a-list-item>{{ item }}</a-list-item>
                    </template>
                  </a-list>
                </section>
                <section>
                  <h2>建议命令</h2>
                  <a-table
                    size="small"
                    :pagination="false"
                    :columns="commandColumns"
                    :data-source="result.commands"
                    row-key="command"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'command'">
                        <code class="command-code">{{ record.command }}</code>
                      </template>
                      <template v-else-if="column.key === 'riskLevel'">
                        <a-tag :color="riskColor(record.riskLevel)">
                          {{ record.riskLevel || 'LOW' }}
                        </a-tag>
                      </template>
                      <template v-else-if="column.key === 'warning'">
                        <a-alert
                          v-if="record.warning || isHighRisk(record.riskLevel)"
                          :type="isCriticalRisk(record.riskLevel) ? 'error' : 'warning'"
                          show-icon
                          :message="record.warning || fixedRiskWarning"
                        />
                        <span v-else>-</span>
                      </template>
                    </template>
                  </a-table>
                </section>
                <section>
                  <h2>修复步骤</h2>
                  <a-list size="small" :data-source="result.fixSteps">
                    <template #renderItem="{ item }">
                      <a-list-item>{{ item }}</a-list-item>
                    </template>
                  </a-list>
                </section>
              </div>
            </div>

            <div class="follow-up-panel">
              <div class="follow-up-title">继续追问</div>
              <div class="follow-up-controls">
                <a-textarea
                  v-model:value="followUpContent"
                  :rows="2"
                  placeholder="补充新的日志、命令输出或排查结果"
                />
                <a-button type="primary" :loading="followUpLoading" @click="submitFollowUp">
                  发送
                </a-button>
                <a-button :loading="saveCaseLoading" @click="saveAsCase">
                  保存案例
                </a-button>
                <a-button :loading="reportLoading" @click="generateReport">
                  生成复盘
                </a-button>
              </div>
            </div>
          </template>
        </a-card>
      </a-col>
    </a-row>

    <a-drawer
      v-model:open="reportOpen"
      width="min(960px, 100vw)"
      :title="report?.title || '复盘报告'"
      :destroy-on-close="true"
    >
      <a-empty v-if="!report" description="暂无报告" />
      <template v-else>
        <div class="report-actions">
          <a-button type="primary" @click="copyReport">复制 Markdown</a-button>
        </div>
        <pre class="message-content">{{ report.content }}</pre>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { ThunderboltOutlined } from '@ant-design/icons-vue';
import { createCaseFromSession } from '@/api/case';
import { analyzeDiagnosis, continueDiagnosisSession, type AnalyzeResponse } from '@/api/diagnosis';
import { defaultCategoryOptions, fetchFaultCategoryOptions } from '@/api/fault-category';
import { createReportFromSession, type ReportDetail } from '@/api/report';

const loading = ref(false);
const followUpLoading = ref(false);
const saveCaseLoading = ref(false);
const reportLoading = ref(false);
const reportOpen = ref(false);
const followUpContent = ref('');
const result = ref<AnalyzeResponse | null>(null);
const report = ref<ReportDetail | null>(null);

const form = reactive({
  title: 'Docker 执行 docker ps 权限不足',
  category: 'Docker',
  environment: 'Ubuntu 24.04 + Docker',
  isProduction: false,
  urgencyLevel: 'NORMAL',
  description: '执行 docker ps 失败',
  commandOutput: 'permission denied while trying to connect to the Docker daemon socket',
});

const categoryOptions = ref(defaultCategoryOptions);

const urgencyOptions = [
  { label: '普通', value: 'NORMAL' },
  { label: '紧急', value: 'URGENT' },
  { label: '严重', value: 'CRITICAL' },
];

const commandColumns = [
  { title: '命令', dataIndex: 'command', key: 'command', width: 220 },
  { title: '说明', dataIndex: 'description', key: 'description' },
  { title: '风险', dataIndex: 'riskLevel', key: 'riskLevel', width: 100 },
  { title: '风险说明', dataIndex: 'warning', key: 'warning', width: 280 },
];

const fixedRiskWarning = '该命令可能影响生产环境，请确认已备份数据，并明确知道命令含义后再执行。';
const riskText = computed(() => `当前建议包含 ${result.value?.riskLevel} 风险命令，系统不会自动执行任何命令。`);
const riskDescription = computed(() => {
  const warnings = result.value?.riskWarnings || [];
  return [fixedRiskWarning, ...warnings].filter(Boolean).join(' ');
});
const riskAlertType = computed(() => (result.value?.riskLevel === 'CRITICAL' ? 'error' : 'warning'));

onMounted(loadCategoryOptions);

async function loadCategoryOptions() {
  try {
    const options = await fetchFaultCategoryOptions();
    categoryOptions.value = options.map((item) => ({ label: item.categoryName, value: item.categoryName }));
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载故障分类失败');
  }
}

async function submitAnalyze() {
  loading.value = true;
  try {
    result.value = await analyzeDiagnosis({
      ...form,
      logContent: form.commandOutput,
    });
  } catch (error) {
    message.error('分析失败，请检查后端服务是否启动');
  } finally {
    loading.value = false;
  }
}

async function submitFollowUp() {
  if (!result.value?.sessionId) {
    message.warning('请先完成一次分析');
    return;
  }
  if (!followUpContent.value.trim()) {
    message.warning('请输入追问内容');
    return;
  }
  const sessionId = result.value.sessionId;
  followUpLoading.value = true;
  try {
    result.value = await continueDiagnosisSession(sessionId, {
      content: followUpContent.value.trim(),
    });
    followUpContent.value = '';
    message.success('分析已更新');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '继续分析失败');
  } finally {
    followUpLoading.value = false;
  }
}

async function saveAsCase() {
  if (!result.value?.sessionId) {
    message.warning('请先完成一次分析');
    return;
  }
  saveCaseLoading.value = true;
  try {
    await createCaseFromSession(result.value.sessionId, {
      status: 'DRAFT',
      tags: [form.category].filter(Boolean),
    });
    message.success('已保存为案例');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存案例失败');
  } finally {
    saveCaseLoading.value = false;
  }
}

async function generateReport() {
  if (!result.value?.sessionId) {
    message.warning('请先完成一次分析');
    return;
  }
  reportLoading.value = true;
  try {
    report.value = await createReportFromSession(result.value.sessionId);
    reportOpen.value = true;
    message.success('复盘报告已生成');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '生成复盘失败');
  } finally {
    reportLoading.value = false;
  }
}

async function copyReport() {
  if (!report.value?.content) {
    return;
  }
  try {
    await navigator.clipboard.writeText(report.value.content);
    message.success('Markdown 已复制');
  } catch {
    message.error('复制失败，请手动选择内容复制');
  }
}

function isCriticalRisk(level?: string) {
  return level === 'CRITICAL';
}

function isHighRisk(level?: string) {
  return level === 'HIGH' || level === 'CRITICAL';
}

function riskColor(level?: string) {
  const colors: Record<string, string> = {
    LOW: 'green',
    MEDIUM: 'orange',
    HIGH: 'red',
    CRITICAL: 'volcano',
  };
  return level ? colors[level] || 'default' : 'default';
}
</script>

<style scoped>
.risk-alert {
  margin-top: 12px;
}

.command-code {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
