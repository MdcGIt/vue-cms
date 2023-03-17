<template>
  <div class="">
    <el-row class="mb8">
      <el-button plain
                 type="success"
                 icon="el-icon-edit"
                 size="mini"
                 :disabled="!this.siteId"
                 @click="handleSave">保存</el-button>
    </el-row>
    <el-form ref="form"
              :model="form"
              v-loading="loading"
              :disabled="!this.siteId"
              label-width="140px"
              label-suffix="：">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>默认模板配置</span>
        </div>
        <el-tabs v-model="publishPipeActiveName">
          <el-tab-pane 
            v-for="pp in this.form.publishPipeProps"
            :key="pp.pipeCode"
            :command="pp"
            :name="pp.pipeCode"
            :label="pp.pipeName">
            <el-form-item 
              label="栏目列表页模板"
              prop="listTemplate">
              <el-input v-model="pp.props.defaultListTemplate">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate('defaultListTemplate')">选择</el-button>
              </el-input>
              <el-button 
                class="ml5"
                icon="el-icon-finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog('defaultListTemplate')">应用到栏目</el-button>
            </el-form-item>
            <el-form-item 
              v-for="ct of contentTypes" 
              :key="ct.id" 
              :command="ct"
              :label="ct.name + '详情页模板'"
              :prop="`defaultDetailTemplate_${ct.id}`">
              <el-input v-model="pp.props[`defaultDetailTemplate_${ct.id}`]">
                <el-button 
                  slot="append"
                  type="primary"
                  @click="handleSelectTemplate(`defaultDetailTemplate_${ct.id}`)">选择</el-button>
              </el-input>
              <el-button 
                class="ml5"
                icon="el-icon-finished" 
                type="primary" 
                plain 
                @click="handleApplyToCatalog(`defaultDetailTemplate_${ct.id}`)">应用到栏目</el-button>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-form>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open.sync="openCatalogSelector"
      multiple
      @ok="doApplyToCatalog"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 模板选择组件 -->
    <cms-template-selector :open="openTemplateSelector" 
                       :publishPipeCode="publishPipeActiveName"
                       @ok="handleTemplateSelected"
                       @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script>
import { getDefaultTemplates, saveDefaultTemplates, applyDefaultTemplate } from "@/api/contentcore/site";
import { getContentTypes } from "@/api/contentcore/catalog";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";

export default {
  name: "CMSSiteDefaultTemplate",
  components: {
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-template-selector': CMSTemplateSelector
  },
  props: {
    site: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  watch: {
    site(newVal) { 
      this.siteId = newVal;
    },
    siteId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadDefaultTemplates();
      }
    },
  },
  data () {
    return {
      loading: false,
      siteId: this.site,
      contentTypes: [],
      openCatalogSelector: false,
      publishPipeActiveName: "",
      openTemplateSelector: false,
      templatePropKey: "",
      form: {
      }
    };
  },
  created() {
    this.loadContentTypes();
    this.loadDefaultTemplates();
  },
  methods: {
    loadContentTypes() {
      getContentTypes().then(response => {
        this.contentTypes = response.data;
      });
    },
    loadDefaultTemplates () {
      this.loading = true;
      const params = { siteId: this.siteId };
      getDefaultTemplates(params).then(response => {
        this.form = response.data;
        if (this.form.publishPipeProps.length > 0) {
          this.publishPipeActiveName = this.form.publishPipeProps[0].pipeCode;
        }
        this.loading = false;
      });
    },
    handleSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          saveDefaultTemplates(this.form).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("保存成功");
            }
          });
        }
      });
    },
    handleSelectTemplate(propKey) {
      this.templatePropKey = propKey;
      this.openTemplateSelector = true;
    },
    handleTemplateSelected(template) {
      this.form.publishPipeProps.map(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          item.props[this.templatePropKey] = template;
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel() {
      this.openTemplateSelector = false;
    },
    handleApplyToCatalog(propKey) {
      this.openCatalogSelector = true;
      this.templatePropKey = propKey;
    },
    doApplyToCatalog (catalogs) {
      if (catalogs.length == 0) {
        this.$modal.msgWarning("请先选择栏目");
        return;
      }
      let data = {
        siteId: this.siteId,
        toCatalogIds: catalogs.map(c => c.id)
      }
      this.form.publishPipeProps.forEach(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          data.publishPipeProps = [{ pipeCode: item.pipeCode, props: {} }];
          data.publishPipeProps[0].props[this.templatePropKey] = "";
        }
      });
      applyDefaultTemplate(data).then(res => {
        this.$modal.msgSuccess(res.msg);
        this.openCatalogSelector = false;
      });
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    }
  }
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 18px;
  width: 600px;
}
.el-input, .el-select {
  width: 220px;
}
.el-card {
  margin-bottom: 10px;
}
</style>