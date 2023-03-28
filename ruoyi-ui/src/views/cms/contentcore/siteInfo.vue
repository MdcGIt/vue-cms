<template>
  <div class="">
    <el-row :gutter="10"
                class="mb8">
      <el-col :span="1.5">
        <el-button plain
                    type="success"
                    icon="el-icon-edit"
                    size="mini"
                    :disabled="!this.siteId"
                    @click="handleUpdate">保存</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain
                    type="primary"
                    size="mini"
                    :disabled="!this.siteId"
                    @click="handlePreview"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>预览</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain
                    type="primary"
                    icon="el-icon-s-promotion"
                    size="mini"
                    :disabled="!this.siteId"
                    @click="handlePublish">发布首页</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown>
          <el-button split-button
                      plain
                      size="mini" 
                      type="primary"
                      icon="el-icon-s-promotion"
                      :disabled="!this.siteId">
            发布全站<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(30)">发布已发布的内容</el-dropdown-item>
            <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(20)">发布待发布的内容</el-dropdown-item>
            <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(0)">发布初稿的内容</el-dropdown-item>
            <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(60)">发布重新编辑的内容</el-dropdown-item>
            <el-dropdown-item icon="el-icon-s-promotion" @click.native="handlePublishAll(40)">发布已下线的内容</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
    </el-row>
    <el-form ref="form_info"
              :model="form_info"
              v-loading="loading"
              :rules="rules"
              :disabled="!this.siteId"
              label-width="130px"
              label-suffix="：">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>基础属性</span>
        </div>
        <el-form-item label="站点ID"
                      prop="siteId">
          <span class="span_siteId" v-if="form_info.siteId!=undefined">{{form_info.siteId}}</span>
        </el-form-item>
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="form_info.name" />
        </el-form-item>
        <el-form-item label="目录"
                      prop="path">
          <el-input v-model="form_info.path" disabled />
        </el-form-item>
        <el-form-item label="资源域名"
                      prop="resourceUrl">
          <el-input v-model="form_info.resourceUrl"
                  placeholder="http(s)://" />
        </el-form-item>
        <el-form-item label="站点Logo" prop="logo">
          <cms-logo-view v-model="form_info.logo" :src="form_info.logoSrc"
                          :width="218" :height="150"></cms-logo-view>
        </el-form-item>
        <el-form-item label="描述"
                      prop="description">
          <el-input v-model="form_info.description"
                    type="textarea"
                    maxlength="100" />
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>发布通道属性</span>
        </div>
        <el-tabs v-if="form_info.publishPipeDatas&&form_info.publishPipeDatas.length>0"
                  v-model="publishPipeActiveName">
          <el-tab-pane v-for="pp in this.form_info.publishPipeDatas"
                        :key="pp.pipeCode"
                        :name="pp.pipeCode"
                        :label="pp.pipeName">
            <el-form-item label="URL">
              <el-input v-model="pp.props.url" placeholder="http(s)://www.test.com/" />
            </el-form-item>
            <el-form-item label="静态文件类型">
              <el-select v-model="pp.props.staticSuffix" size="small">
                <el-option
                  v-for="dict in dict.type.CMSStaticSuffix"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="首页模板">
              <el-input v-model="pp.props.indexTemplate">
                <el-button slot="append"
                          type="primary"
                          @click="handleSelectTemplate()">选择</el-button>
              </el-input>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
        <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
          <i class="el-icon-info mr5"></i>预览/发布需要先添加发布通道，
          <el-button type="text" @click="handleGoPublishPipe">去添加</el-button>
        </div>
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
          :pk="siteId">
        </cms-exmodel-editor>
      </el-card>
    </el-form>
    <!-- 模板选择组件 -->
    <cms-template-selector :open="openTemplateSelector" 
                       :publishPipeCode="publishPipeActiveName" 
                       @ok="handleTemplateSelected"
                       @cancel="handleTemplateSelectorCancel" />
    <!-- 进度条 -->
    <cms-progress title="发布任务" :open.sync="openProgress" :taskId.sync="taskId"></cms-progress>
  </div>
</template>
<style scoped>
.el-form-item {
  margin-bottom: 12px;
  width: 460px;
}
.el-card {
  margin-bottom: 10px;
}
</style>
<script>
import { getSite, publishSite, updateSite  } from "@/api/contentcore/site";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSProgress from '@/views/components/Progress';
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSEXModelEditor from '@/views/cms/components/EXModelEditor';

export default {
  name: "CMSSiteInfo",
  components: {
    'cms-template-selector': CMSTemplateSelector,
    'cms-progress': CMSProgress,
    "cms-logo-view": CMSLogoView,
    "cms-exmodel-editor": CMSEXModelEditor
  },
  dicts: ['CMSStaticSuffix'],
  computed: {
    showEXModel() {
      return this.form_info.configProps && this.form_info.configProps.ExtendModel != null && this.form_info.configProps.ExtendModel.length > 0;
    }
  },
  props: {
    site: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      openTemplateSelector: false,
      publishPipeActiveName: "",

      openProgress: false,
      taskId: "",
      siteId: this.site,
      form_info: {
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9]*$", message: "不能为空且只能使用大小写字母和数字", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    site(newVal) { 
      this.siteId = newVal;
    },
    siteId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadSiteInfo();
      }
    },
  },
  created() {
    this.loadSiteInfo();
  },
  methods: {
    loadSiteInfo () {
      if (!this.siteId) {
        this.$modal.msgError("站点ID错误：" + this.siteId);
        return;
      }
      this.loading = true;
      getSite(this.siteId).then(response => {
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
          updateSite(this.form_info).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("保存成功");
            } else {
              this.$modal.msgError(response.msg);
            }
          });
        }
      });
    },
    handleSelectTemplate () {
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.form_info.publishPipeDatas.map(item => {
        if (item.pipeCode == this.publishPipeActiveName) {
          item.props.indexTemplate = template;
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleGoPublishPipe () {
      this.$router.push({
        path: "/cms/publishpipe"
      });
    },
    handlePreview () {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "site", dataId: this.siteId },
      });
      window.open(routeData.href, '_blank');
    },
    handlePublish () {
      publishSite({ siteId: this.siteId, publishIndex: true }).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess("发布成功");
        }
      });
    },
    handlePublishAll (status) {
      const params = { siteId: this.siteId, publishIndex: false, contentStatus: status }
      publishSite(params).then(response => {
        if (response.code == 200) {
          if (response.data && response.data != "") {
            this.taskId = response.data;
            this.openProgress = true;
          }
        } else {
          this.$modal.msgError(response.msg);
        }
      });
    }
  }
};
</script>