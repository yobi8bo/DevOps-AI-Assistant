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

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="11">
        <a-card title="故障输入" :bordered="false">
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
        <a-card title="AI 分析结果" :bordered="false">
          <a-empty v-if="!result" description="提交后将在这里显示结构化排障建议" />
          <div v-else class="result">
            <a-alert v-if="result.riskLevel !== 'LOW'" type="warning" show-icon :message="riskText" />
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
              <a-table size="small" :pagination="false" :columns="commandColumns" :data-source="result.commands" row-key="command" />
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
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { ThunderboltOutlined } from '@ant-design/icons-vue';
import { analyzeDiagnosis, type AnalyzeResponse } from '@/api/diagnosis';

const loading = ref(false);
const result = ref<AnalyzeResponse | null>(null);

const form = reactive({
  title: 'Docker 执行 docker ps 权限不足',
  category: 'Docker',
  environment: 'Ubuntu 24.04 + Docker',
  isProduction: false,
  urgencyLevel: 'NORMAL',
  description: '执行 docker ps 失败',
  commandOutput: 'permission denied while trying to connect to the Docker daemon socket',
});

const categoryOptions = [
  'Linux',
  'Docker',
  'Kubernetes',
  'Jenkins',
  'Nginx',
  'MySQL',
  'Redis',
  'Spring Boot',
].map((value) => ({ label: value, value }));

const urgencyOptions = [
  { label: '普通', value: 'NORMAL' },
  { label: '紧急', value: 'URGENT' },
  { label: '严重', value: 'CRITICAL' },
];

const commandColumns = [
  { title: '命令', dataIndex: 'command', key: 'command' },
  { title: '说明', dataIndex: 'description', key: 'description' },
  { title: '风险', dataIndex: 'riskLevel', key: 'riskLevel', width: 100 },
];

const riskText = computed(() => `当前建议包含 ${result.value?.riskLevel} 风险命令，请确认影响范围后再执行。`);

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
</script>

