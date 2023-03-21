<template>
  <div>
    <el-row :gutter="10"
            class="mb8">
      <el-col :span="1.5">
        <el-button type="success"
                    icon="el-icon-edit"
                    size="mini"
                    plain
                    :disabled="!this.catalogId"
                    @click="handleUpdate">保存</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary"
                    size="mini"
                    plain
                    :disabled="!this.catalogId"
                    @click="handlePreview"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>预览</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown>
          <el-button plain
                      size="mini" 
                      type="primary"
                      icon="el-icon-s-promotion"
                      :disabled="!this.catalogId"
                      @click="handlePublish(-1)">
            发布<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-for="dict in dict.type.CMSContentStatus" :key="dict.value" icon="el-icon-s-promotion" @click.native="handlePublish(dict.value)">发布{{dict.label}}的内容</el-dropdown-item>
            
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success"
                    icon="el-icon-edit"
                    size="mini"
                    plain
                    :disabled="!this.catalogId"
                    @click="handleChangeVisible">{{ catalogVisible ? "隐藏" : "显示" }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-popconfirm title="删除栏目会删除包括子栏目下所有数据确定删除吗？"
                        @confirm="handleDelete">
            <el-button type="danger" 
                        icon="el-icon-delete"
                        size="mini"
                        plain
                        :disabled="!this.catalogId"
                        slot="reference">删除</el-button>
          </el-popconfirm>
      </el-col>
    </el-row>
    <el-form ref="form_info"
              v-loading="loading"
              :model="form_info"
              :rules="rules"
              :disabled="!this.catalogId"
              label-width="140px"
              label-suffix="：">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>基础属性</span>
        </div>
        <el-form-item label="栏目ID"
                      prop="catalogId">
          <span class="span_catalogid" v-if="form_info.catalogId!=undefined">{{form_info.catalogId}}</span>
        </el-form-item>
        <el-form-item label="栏目名称"
                      prop="name">
          <el-input v-model="form_info.name" />
        </el-form-item>
        <el-form-item label="栏目别名"
                      prop="alias">
          <el-input v-model="form_info.alias" />
        </el-form-item>
        <el-form-item label="栏目目录"
                      prop="path">
          <el-input v-model="form_info.path" />
        </el-form-item>
        <el-form-item label="栏目类型"
                      prop="catalogType">
          <el-select v-model="form_info.catalogType"
                    placeholder="栏目类型">
            <el-option v-for="ct in catalogTypeOptions"
                      :key="ct.id"
                      :label="ct.name"
                      :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item
          label="链接地址"
          v-if="form_info.catalogType==='link'"
          prop="redirectUrl">
          <el-input v-model="form_info.redirectUrl" placeholder="http(s)://">
            <el-dropdown slot="append" @command="handleLinkTo">
              <el-button>
                内部链接<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="content">选择内容</el-dropdown-item>
                <el-dropdown-item command="catalog">选择栏目</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-input>
        </el-form-item>
        <el-form-item label="栏目描述"
                      prop="description">
          <el-input v-model="form_info.description"
                    type="textarea"
                    maxlength="100" />
        </el-form-item>
        <el-form-item label="栏目Logo" prop="logo">
          <cms-logo-view v-model="form_info.logo" :src="form_info.logoSrc" :height="150"></cms-logo-view>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>发布通道属性</span>
        </div>
        <el-tabs v-model="publishPipeActiveName">
          <el-tab-pane v-for="pp in this.form_info.publishPipeDatas"
                        :key="pp.pipeCode"
                        :command="pp"
                        :name="pp.pipeCode"
                        :label="pp.pipeName">
            <el-form-item label="首页模板"
                          prop="indexTemplate">
              <el-input v-model="pp.props.indexTemplate">
                <el-button slot="append"
                          type="primary"
                          @click="handleSelectTemplate('indexTemplate')">选择</el-button>
              </el-input>
            </el-form-item>
            <el-form-item label="列表页模板"
                          prop="listTemplate">
              <el-input v-model="pp.props.listTemplate">
                <el-button slot="append"
                          type="primary"
                          @click="handleSelectTemplate('listTemplate')">选择</el-button>
              </el-input>
              <el-button class="ml5"
                         icon="el-icon-bottom-right" 
                         type="primary" 
                         plain 
                         @click="handleApplyToChildren('listTemplate')">应用到子栏目</el-button>
            </el-form-item>
            <el-form-item v-for="ct of contentTypes" 
                          :key="ct.id" 
                          :command="ct"
                          :label="ct.name + '详情页模板'"
                          :prop="`detailTemplate_${ct.id}`">
              <el-input v-model="pp.props[`detailTemplate_${ct.id}`]">
                <el-button slot="append"
                          type="primary"
                          @click="handleSelectTemplate(`detailTemplate_${ct.id}`)">选择</el-button>
              </el-input>
              <el-button class="ml5"
                         icon="el-icon-bottom-right" 
                         type="primary" 
                         plain 
                         @click="handleApplyToChildren(`detailTemplate_${ct.id}`)">应用到子栏目</el-button>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>SEO属性</span>
        </div>
        <el-form-item label="SEO标题"
                      prop="seoTitle">
          <el-input v-model="form_info.seoTitle"
                    placeholder="请输入SEO标题" />
        </el-form-item>
        <el-form-item label="SEO关键字"
                      prop="seoKeywords">
          <el-input v-model="form_info.seoKeywords"
                    placeholder="请输入SEO关键字" />
        </el-form-item>
        <el-form-item label="SEO描述"
                      prop="seoDescription">
          <el-input v-model="form_info.seoDescription"
                    type="textarea"
                    maxlength="100"
                    placeholder="请输入SEO描述" />
        </el-form-item>
      </el-card>
      <el-card v-if="showEXModel" shadow="hover">
        <div slot="header" class="clearfix">
          <span>扩展模型属性</span>
        </div>
        
        <cms-exmodel-editor 
          ref="EXModelEditor"
          :xmodel="form_info.configProps.ExtendModel" 
          :pk="catalogId">
        </cms-exmodel-editor>
      </el-card>
    </el-form>
    <el-dialog 
      title="发布栏目"
      :visible.sync="publishDialogVisible"
      width="500px"
      class="publish-dialog">
      <div>
        <p>提示：</p>
        <p>本次生成的静态页面将覆盖原有页面，确认生成所有静态页面吗？</p>
        <el-checkbox v-model="publishChild">包含子栏目</el-checkbox>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleDoPublish">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector :open="openTemplateSelector" 
                       :publishPipeCode="publishPipeActiveName"
                       @ok="handleTemplateSelected"
                       @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <!-- 进度条 -->
    <cms-progress title="发布任务" :open.sync="openProgress" :taskId="taskId"></cms-progress>
  </div>
</template>
<script>
import * as catalogApi from "@/api/contentcore/catalog";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSProgress from '@/views/components/Progress';
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSEXModelEditor from '@/views/cms/components/EXModelEditor';

export default {
  name: "CMSCatalogInfo",
  components: {
    "cms-exmodel-editor": CMSEXModelEditor,
    'cms-template-selector': CMSTemplateSelector,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    'cms-progress': CMSProgress,
    "cms-logo-view": CMSLogoView
  },
  dicts: ['CMSStaticSuffix', 'CMSContentStatus'],
  props: {
    cid: {
      type: String, 
      default: undefined,
      required: false,
    },
  },
  computed: {
    showEXModel() {
      return this.form_info.configProps && this.form_info.configProps.ExtendModel != null && this.form_info.configProps.ExtendModel.length > 0;
    },
    catalogVisible() {
      return this.form_info.visibleFlag == "0";
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      activeName: 'basicInfo',
      openCatalogSelector: false,
      openContentSelector: false,
      openTemplateSelector: false, // 是否显示模板选择弹窗
      propKey: "", // 选择模板时记录变更的模板对应属性Key
      openProgress: false, // 是否显示任务进度条
      taskId: "", // 任务ID
      // 发布选项弹窗
      publishDialogVisible: false,
      publishChild: false,
      publishStatus: -1,
      publishPipeActiveName: "pc", // 当前选中的发布通道Tab
      catalogId: parseInt(this.cid),
      // 栏目信息表单
      form_info: {
        siteId: ""
      },
      catalogTypeOptions: [],
      publishPipes: [], // 栏目发布通道数据
      // 表单校验
      rules: {
        name: [
          { required: true, message: "栏目名称不能为空", trigger: "blur" }
        ],
        alias: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ],
        catalogType: [
          { required: true, message: "栏目类型不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.loadCatalogTypes();
    this.loadContentTypes();
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      if (newVal && newVal > 0) {
        this.loadCatalogInfo();
      } else {
        this.form_info = { siteId: "" };
      }
    }
  },
  methods: {
    loadContentTypes() {
      catalogApi.getContentTypes().then(response => {
        this.contentTypes = response.data;
      });
    },
    loadCatalogTypes () {
      catalogApi.getCatalogTypes().then(response => {
        this.catalogTypeOptions = response.data;
      });
    },
    loadCatalogInfo () {
      if (!this.catalogId) {
        this.$modal.msgError("请先选择一个栏目");
        return;
      }
      this.loading = true;
      catalogApi.getCatalogData(this.catalogId).then(response => {
        this.form_info = response.data;
        if (this.form_info.publishPipeDatas.length > 0) {
          this.publishPipeActiveName = this.form_info.publishPipeDatas[0].pipeCode;
        }
        this.loading = false;
      });
    },
    handleUpdate () {
      this.$refs["form_info"].validate(valid => {
        if (valid) {
          if (this.showEXModel) {
            this.form_info.params = this.$refs.EXModelEditor.getDatas();
          }
          catalogApi.updateCatalog(this.form_info).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("保存成功");
              this.$emit("update", response.data);
            } else {
              this.$modal.msgError(response.msg);
            }
          });
        }
      });
    },
    handleChangeVisible () {
        const visible = this.form_info.visibleFlag == "1" ? "0" : "1";
        catalogApi.changeVisible(this.form_info.catalogId, visible).then(response => {
          if (response.code === 200) {
              this.$modal.msgSuccess(response.msg);
              this.form_info.visibleFlag = visible;
          } else {
              this.$modal.msgError(response.msg);
          }
        });
    },
    handlePreview () {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "catalog", dataId: this.form_info.catalogId },
      });
      window.open(routeData.href, '_blank');
    },
    handlePublish (publishStatus) {
      this.publishStatus = publishStatus;
      this.publishDialogVisible = true;
    },
    handleDoPublish() {
      const data = {
        catalogId: this.form_info.catalogId,
        publishDetail: this.publishStatus != -1,
        publishStatus: this.publishStatus,
        publishChild: this.publishChild
      };
      this.publishDialogVisible = false;
      this.publishChild = false;
      catalogApi.publishCatalog(data).then(response => {
        if (response.code == 200) {
          if (response.data && response.data != "") {
            this.taskId = response.data;
            this.openProgress = true;
          }
        } else {
          this.$modal.msgError(response.msg);
        }
      }); 
    },
    handleDelete () {
      if (!this.catalogId) {
        this.msgError("请先选择一个栏目");
        retrun;
      }
      catalogApi.delCatalog(this.catalogId).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("删除成功");
          this.resetForm("form_info");
          this.$emit("remove", this.catalogId);
        } else {
          this.$modal.msgError(response.msg);
        }
      });
    },
    handleSelectTemplate (propKey) {
      this.propKey = propKey;
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.form_info.publishPipeDatas.map(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          item.props[this.propKey] = template;
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleApplyToChildren (propKey) {
      const data = { 
        catalogId: this.catalogId,
        publishPipeCode: this.publishPipeActiveName,
        publishPipePropKeys: [ propKey ]
       }
       catalogApi.applyToChildren(data).then(res => {
         this.$modal.msgSuccess(res.msg);
       });
    },
    handleLinkTo(type) {
      if (type === 'content') {
        this.openContentSelector = true;
      } else if (type === 'catalog') {
        this.openCatalogSelector = true;
      }
    },
    handleCatalogSelectorOk(catalogs) {
      if (catalogs && catalogs.length > 0) {
        this.form_info.redirectUrl = catalogs[0].internalUrl;
      }
      this.openCatalogSelector = false;
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.form_info.redirectUrl = contents[0].internalUrl;
      }
      this.openContentSelector = false;
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    }
  }
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 18px;
  width: 620px;
}
.el-input, .el-select, .el-textarea {
  width: 330px;
}
.el-card {
  margin-bottom: 10px;
}
</style>