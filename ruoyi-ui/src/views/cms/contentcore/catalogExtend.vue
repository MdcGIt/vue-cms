<template>
  <div class="catalog-extend-container">
    <el-row class="mb8">
      <el-button plain
                 type="success"
                 icon="el-icon-edit"
                 size="mini"
                 :disabled="!this.catalogId"
                 @click="handleSaveExtends">保存</el-button>
      <el-button plain 
                 type="primary" 
                 icon="el-icon-bottom-right" 
                 size="mini"
                 :disabled="!this.catalogId"
                 @click="handleApplyAllToCatalog()">应用到子栏目</el-button>
    </el-row>
    <el-form ref="form_extend"
              :model="form_extend"
              v-loading="loading"
              :disabled="!this.catalogId"
              label-width="140px"
              label-suffix="：">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>基础配置</span>
        </div>
        <el-form-item label="是否开启索引"
                      prop="EnableIndex">
          <el-switch
            v-model="form_extend.EnableIndex"
            active-text="开启"
            inactive-text="关闭"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item label="栏目扩展模型"
                      prop="CatalogExtendModel">
          <el-select 
            v-model="form_extend.CatalogExtendModel" 
            filterable 
            clearable 
            placeholder="请选择">
            <el-option
              v-for="item in exmodelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <el-button class="ml5 btn-apply-child"
                      icon="el-icon-finished" 
                      type="primary" 
                      plain 
                      @click="handleApplyToCatalog('CatalogExtendModel')">应用到栏目</el-button>
        </el-form-item>
        <el-form-item label="内容扩展模型"
                      prop="ContentExtendModel">
          <el-select 
            v-model="form_extend.ContentExtendModel" 
            filterable 
            clearable 
            placeholder="请选择">
            <el-option
              v-for="item in exmodelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <el-button class="ml5 btn-apply-child"
                      icon="el-icon-finished" 
                      type="primary" 
                      plain 
                      @click="handleApplyToCatalog('ContentExtendModel')">应用到栏目</el-button>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>内容配置</span>
        </div>
        <el-form-item 
          label="文章正文图片尺寸">
          宽：<el-input v-model="form_extend.ArticleImageWidth" style="width:100px"></el-input>
          高：<el-input v-model="form_extend.ArticleImageHeight" style="width:100px"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>词汇配置</span>
        </div>
        <el-form-item 
          label="热词分组"
          prop="HotWordGroups">
          <el-checkbox-group v-model="form_extend.HotWordGroups">
            <el-checkbox v-for="group in hotWordGroups" :label="group.code" :key="group.code">{{ group.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-card>
    </el-form>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open.sync="openCatalogSelector"
      multiple
      @ok="doApplyToCatalog"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
  </div>
</template>
<script>
import { getCatalogExtends, saveCatalogExtends, applyToChildren } from "@/api/contentcore/catalog";
import { getHotWordGroupOptions } from "@/api/word/hotWord";
import { listXModel } from "@/api/contentcore/exmodel";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";

export default {
  name: "CMSCatalogExtend",
  components: {
    'cms-catalog-selector': CMSCatalogSelector
  },
  props: {
    cid: {
      type: String,
      default: undefined,
      required: true,
    }
  },
  data () {
    return {
      loading: false,
      siteId: undefined,
      catalogId: this.cid,
      openCatalogSelector: false,
      applyConfigPropKey: "",
      exmodelOptions: [],
      repeatCheckOptions: [
        { label: "不校验", value: "0" },
        { label: "全站校验", value: "1" },
        { label: "栏目校验", value: "2" }
        ],
      form_extend: {
      },
      hotWordGroups: []
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadCatalogExtends();
      }
    },
  },
  created() {
    this.loadEXModelList();
    this.loadHotWordGroups();
    this.loadCatalogExtends();
  },
  methods: {
    loadCatalogExtends () {
      this.loading = true;
      const params = { catalogId: this.catalogId };
      getCatalogExtends(params).then(response => {
        this.form_extend = response.data;
        this.siteId = response.data.siteId;
        this.loading = false;
      });
    },
    loadEXModelList() {
      listXModel().then(response => {
        this.exmodelOptions = response.data.rows.map(m => {
          return { label: m.name, value: m.modelId };
        });
      });
    },
    loadHotWordGroups() {
      getHotWordGroupOptions().then(response => {
        if (response.code == 200) {
          this.hotWordGroups = response.data.rows;
        }
      });
    },
    handleSaveExtends () {
      this.$refs["form_extend"].validate(valid => {
        if (valid) {
          const data = {};
          Object.keys(this.form_extend).forEach(key => {
            if (typeof this.form_extend[key] == 'object') {
            data[key] = JSON.stringify(this.form_extend[key]);
            } else {
              data[key] = this.form_extend[key];
            }
          })
          saveCatalogExtends(this.catalogId, data).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("保存成功");
            } else {
              this.$modal.msgError(response.msg);
            }
          });
        }
      });
    },
    handleApplyAllToCatalog() {
      const data = { 
        catalogId: this.catalogId,
        allExtends: true
      }
      this.$modal.loading("请稍后...");
      applyToChildren(data).then(res => {
        this.$modal.closeLoading();
        this.$modal.msgSuccess(res.msg);
      });
    },
    handleApplyToCatalog(propKey) {
      this.openCatalogSelector = true;
      this.applyConfigPropKey = propKey;
    },
    doApplyToCatalog (catalogs) {
      const data = { 
        catalogId: this.catalogId,
        toCatalogIds: catalogs.map(c => c.id),
        configPropKeys: [ this.applyConfigPropKey ]
      }
      applyToChildren(data).then(res => {
        this.$modal.msgSuccess(res.msg);
        this.openCatalogSelector = false;
      });
    },
    handleCatalogSelectorClose() {
      this.applyConfigPropKey = "";
      this.openCatalogSelector = false;
    }
  }
};
</script>
<style>
.catalog-extend-container .el-form-item {
  margin-bottom: 12px;
  width: 600px;
}
.catalog-extend-container .el-card {
  margin-bottom: 10px;
}
.catalog-extend-container .el-input, .el-select, 
.catalog-extend-container .el-input-number  {
  width: 301.5px;
}
.catalog-extend-container .el-upload-list {
  width: 300px;
}
.catalog-extend-container .btn-apply-child {
  padding: 10px;
}
</style>