<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24">
        <div class="grid-btn-bar bg-purple-white">
          <el-button plain type="info" size="mini" icon="el-icon-back" @click="handleGoBack">返回列表</el-button>
          <el-button plain type="success" size="mini" icon="el-icon-edit" @click="handleSave">{{ $t("Common.Save") }}</el-button>
          <el-button plain type="primary" size="mini" icon="el-icon-s-promotion" @click="handlePublish">发布</el-button>
          <el-button plain type="primary" size="mini" @click="handlePreview"><svg-icon icon-class="eye-open" class="mr5"></svg-icon>预览</el-button>
          <el-button plain type="warning" v-if="isLock" size="mini" icon="el-icon-unlock" @click="handleChangeLockState">解锁</el-button>
          <el-button plain type="primary" v-else size="mini" icon="el-icon-lock" @click="handleChangeLockState">锁定</el-button>
        </div>
      </el-col>
    </el-row>
    <el-row class="art-editor-container" :gutter="10" v-loading="loading">
      <el-form ref="form" :model="form" :rules="rules" label-width="70px">
        <el-col :lg="18" :md="16">
          <el-row>
            <el-col class="pr10">
              <el-card shadow="always" class="card-title">
                <div class="art-title bg-purple-white">
                  <el-form-item label="标题" prop="title">
                    <el-input
                      v-model="form.title"
                      maxlength="360"
                      show-word-limit>
                      <el-button
                        slot="append"
                        icon="el-icon-arrow-down"
                        @click="toggleOtherTitle"
                      ></el-button>
                    </el-input>
                  </el-form-item>
                  <el-form-item
                    label="短标题"
                    v-if="showOtherTitle"
                    prop="shortTitle">
                    <el-input v-model="form.shortTitle" maxlength="120" show-word-limit />
                  </el-form-item>
                  <el-form-item
                    label="副标题"
                    v-if="showOtherTitle"
                    prop="subTitle">
                    <el-input v-model="form.subTitle" maxlength="120" show-word-limit />
                  </el-form-item>
                  <el-form-item
                    label="链接内容"
                    prop="linkFlag">
                    <el-checkbox v-model="form.linkFlag" true-label="Y" false-label="N"></el-checkbox>
                  </el-form-item>
                  <el-form-item
                    label="链接地址"
                    v-if="form.linkFlag==='Y'"
                    prop="redirectUrl">
                    <el-input v-model="form.redirectUrl" placeholder="http(s)://" />
                    <el-dropdown @command="handleLinkTo">
                      <el-button
                        size="mini" 
                        type="primary">
                        内部链接<i class="el-icon-arrow-down el-icon--right"></i>
                      </el-button>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="content">选择内容</el-dropdown-item>
                        <el-dropdown-item command="catalog">选择栏目</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </el-form-item>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-row>
            <el-col class="pr10">
              <el-card v-if="xmodelVisible" shadow="always" class="card-exmodel">
                <cms-exmodel-editor 
                  ref="EXModelEditor"
                  :xmodel="form.catalogConfigProps.ContentExtendModel" 
                  :pk="form.contentId">
                </cms-exmodel-editor>
              </el-card>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'article'">
            <el-col class="pr10">
              <el-card shadow="always">
                <el-form-item label="下载图片" prop="downloadRemoteImage">
                  <el-switch
                    v-model="form.downloadRemoteImage"
                    active-value="Y"
                    inactive-value="N" />
                </el-form-item>
              </el-card>
              <el-card shadow="always" class="card-editor">
                <div class="content bg-purple-white">
                  <ckeditor v-model="form.contentHtml" :disabled="false"></ckeditor>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'image'">
            <el-col class="pr10">
              <cms-image-editor v-model="form.imageList" @choose="handleSetLogo"></cms-image-editor>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'audio'">
            <el-col class="pr10">
              <cms-audio-editor v-model="form.audioList" :logo="form.logoSrc"></cms-audio-editor>
            </el-col>
          </el-row>
          <el-row v-if="this.form.linkFlag !== 'Y' && this.contentType === 'video'">
            <el-col class="pr10">
              <cms-video-editor v-model="form.videoList"></cms-video-editor>
            </el-col>
          </el-row>
        </el-col>
        <el-col :lg="6" :md="8">
          <div class="bg-purple-white">
            <el-card shadow="always">
              <el-tabs v-model="activeName" @tab-click="handleTabClick">
                <el-tab-pane label="基本属性" name="basic">
                  <el-form-item label="所属栏目" prop="catalogId">
                    <el-button-group>
                      <el-button plain type="primary" style="width:152px;" disabled>{{ form.catalogName }}</el-button>
                      <el-button type="primary" icon="el-icon-edit" @click="handleCatalogChange"></el-button>
                    </el-button-group>
                  </el-form-item>
                  <el-form-item label="引导图" prop="logo">
                    <cms-logo-view v-model="form.logo" :src="form.logoSrc"
                                   :width="210" :height="150"></cms-logo-view>
                  </el-form-item>
                  <el-form-item label="作者" prop="author">
                    <el-input v-model="form.author" />
                  </el-form-item>
                  <el-form-item label="编辑" prop="editor">
                    <el-input v-model="form.editor" />
                  </el-form-item>
                  <el-form-item label="是否原创" prop="original">
                    <el-switch
                      v-model="form.original"
                      active-value="Y"
                      inactive-value="N"
                    ></el-switch>
                  </el-form-item>
                  <el-form-item label="属性" prop="attributes">
                    <el-checkbox-group v-model="form.attributes">
                      <el-checkbox v-for="dict in dict.type.CMSContentAttribute" :label="dict.value" :key="dict.value">{{ dict.label }}</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                  <el-form-item label="摘要" prop="summary">
                    <el-input type="textarea" v-model="form.summary" :autosize="summaryInputSize" maxlength="500" show-word-limit />
                  </el-form-item>
                  <el-form-item label="标签" prop="tags">
                    <cms-tag-editor v-model="form.tags"></cms-tag-editor>
                  </el-form-item>
                  <el-form-item label="关键词" prop="keywords">
                    <cms-tag-editor v-model="form.keywords"></cms-tag-editor>
                  </el-form-item>
                  <el-form-item label="来源名称" prop="source">
                    <el-input v-model="form.source" />
                  </el-form-item>
                  <el-form-item label="来源地址" prop="sourceUrl">
                    <el-input v-model="form.sourceUrl" placeholder="http(s)://" />
                  </el-form-item>
                  <el-divider></el-divider>
                  <el-form-item label="发布时间" prop="publishDate">
                    <el-date-picker v-model="form.publishDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" style="width:195px;" />
                  </el-form-item>
                  <el-form-item label="下线时间" prop="offlineDate">
                    <el-date-picker v-model="form.offlineDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" style="width:195px;" />
                  </el-form-item>
                  <el-form-item label="发布通道" prop="publishPipe">
                    <el-checkbox-group v-model="form.publishPipe">
                      <el-checkbox v-for="pp in publishPipeTemplates" :label="pp.pipeCode" :key="pp.pipeCode">{{ pp.pipeName }}</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                  <el-form-item label="独立路径">
                    <el-input v-model="form.staticPath" placeholder="请输入相对站点路径..." />
                  </el-form-item>
                  <el-form-item label="独立模板">
                    <el-switch v-model="showTemplate" @change="handleShowTemplateChange" />
                  </el-form-item>
                  <el-form-item v-show="showTemplate" 
                                v-for="pp in publishPipeTemplates" 
                                :label="pp.pipeName" 
                                :key="pp.pipeCode" 
                                :prop="'template_' + pp.value">
                    <el-input v-model="pp.props.template">
                      <el-button slot="append" icon="el-icon-folder-opened" @click="handleSelectTemplate(pp)"></el-button>
                    </el-input>
                  </el-form-item>
                </el-tab-pane>
                <!-- <el-tab-pane label="扩展配置" name="extend">
                </el-tab-pane> -->
              </el-tabs>
            </el-card>
          </div>
        </el-col>
      </el-form>
    </el-row>
    <!-- 素材选择组件 -->
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      :upload-limit="1"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
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
    <cms-progress :title="progressTitle" :open.sync="openProgress" :taskId="taskId" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script>
import { getInitContentEditorData, addContent, saveContent, publishContent, lockContent, unLockContent, moveContent } from "@/api/contentcore/content";
import Sticky from '@/components/Sticky'
import CKEditor5 from '@/components/CKEditor5';
import CMSProgress from '@/views/components/Progress';
import CMSImageEditor from '@/views/cms/imageAlbum/editor';
import CMSAudioEditor from '@/views/cms/media/audioEditor';
import CMSVideoEditor from '@/views/cms/media/videoEditor';
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSEXModelEditor from '@/views/cms/components/EXModelEditor';
import CMSTagEditor from '@/views/cms/components/TagEditor';

import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "CMSContentEditor",
  dicts: ['CMSContentAttribute', 'CMSContentStatus'],
  components: {
    Treeselect, 
    Sticky,
    'cms-template-selector': CMSTemplateSelector,
    'cms-progress': CMSProgress,
    "cms-image-editor": CMSImageEditor,
    "cms-audio-editor": CMSAudioEditor,
    "cms-video-editor": CMSVideoEditor,
    "cms-resource-dialog": CMSResourceDialog,
    "cms-logo-view": CMSLogoView,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    "cms-exmodel-editor": CMSEXModelEditor,
    "cms-tag-editor": CMSTagEditor,
    "ckeditor": CKEditor5
  },
  computed: {
    isLock () {
      return this.form.isLock === 'Y' && this.form.lockUser != '';
    },
    xmodelVisible() {
      return this.form.catalogConfigProps 
        && this.form.catalogConfigProps.ContentExtendModel != null 
        && this.form.catalogConfigProps.ContentExtendModel.length > 0;
    }
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      openProgress: false,
      progressTitle: "",
      taskId: "",
      showOtherTitle: false,
      showTemplate: false,
      activeName: "basic",
      catalogId: this.$route.query.catalogId || '0',
      contentId: this.$route.query.id || '0',
      contentType: this.$route.query.type,
      opType: !this.$route.query.id || this.$route.query.id == '0' ? 'ADD' : 'UPDATE',
      openCatalogSelector: false,
      catalogSelectorFor: undefined,
      openContentSelector: false,
      // 表单参数
      form: {
        attributes: [],
        contentJson: [],
        contentHtml: "",
        downloadRemoteImage: "Y",
        original: "N",
        publishPipe: [],
        imageList: [],
        tags:[],
        keywords:[]
      },
      publishPipeTemplates: [],
      rules: {
        title: [{ required: true, message: "标题不能为空", trigger: "blur" }]
      },
      openResourceDialog: false,
      summaryInputSize: { minRows: 3, maxRows: 6 },
      openTemplateSelector: false, // 模板选择弹窗
      publishPipeActiveName: "",
    };
  },
  created() {
    console.log("opType", this.opType)
    this.initData();
  },
  methods: {
    handleTabClick(tab, event) {
    },
    toggleOtherTitle() {
      this.showOtherTitle = !this.showOtherTitle;
    },
    onEditorReady( editor )  {
      console.log("ckeditor", "onready");
      // 设置下编辑器最小高度
      editor.editing.view.change(writer => {
        writer.setStyle('min-height', '800px', editor.editing.view.document.getRoot());
      });
      // 图片上传
      editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new ImageUploadAdapter(loader, process.env.VUE_APP_BASE_API + "/cms/resource/upload")
      }
    },
    initData() {
      this.loading = true;
      getInitContentEditorData(this.catalogId, this.contentType, this.contentId).then(response => {
        this.loading = false;
        response.data.attributes = response.data.attributes || [];
        response.data.tags = response.data.tags || [];
        response.data.keywords = response.data.keywords || [];
        this.form = response.data;
        this.catalogId = this.form.catalogId;
        this.contentId = this.form.contentId;
        this.contentType = this.form.contentType;
        this.publishPipeTemplates = this.form.publishPipeTemplates;
        this.showOtherTitle = 'Y' === this.form.showSubTitle || (this.form.shortTitle && this.form.shortTitle.length > 0) 
          || (this.form.subTitle && this.form.subTitle.length > 0);
        this.publishPipeTemplates.forEach(pp => {
          if (pp.props.template != null && pp.props.template != "") {
            this.showTemplate = true;
          }
        });
      });
    },
    handleShowOtherTitle() {
      this.showOtherTitle = !this.showOtherTitle;
    },
    handleGoBack() {
      const obj = { path: "/configs/content" };
      this.$tab.closeOpenPage(obj);
    },
    // 表单重置
    reset() {
      this.form = {};
      this.resetForm("form");
    },
    handleShowTemplateChange() {
      if (!this.showTemplate) {
        this.publishPipeTemplates.forEach((pp, i) => {
          pp.props.template = "";
        });
      }
    },
    handleSelectTemplate(publishPipe) {
      this.publishPipeActiveName = publishPipe.pipeCode;
      this.$nextTick(() => {
        this.openTemplateSelector = true;
      })
    },
    handleTemplateSelected (template) {
      this.publishPipeTemplates.map(pp => {
        if (pp.pipeCode == this.publishPipeActiveName) {
          pp.props.template = template;
        }
      });
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handleSetLogo(path, src) {
      this.$set(this.form, "logoSrc", src);
      this.form.logo = path;
    },
    handleLogoClick() {
      this.openResourceDialog = true;
    },
    handleResourceDialogOk (results) {
      if (results && results.length == 1) {
        this.form.logo = results[0].path;
        this.form.logoSrc = results[0].src;
      }
    },
    /** 提交 */
    handleSave: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.template = {};
          this.publishPipeTemplates.map(item => {
            this.form.template[item.pipeCode] = item.props.template;
          });
          if (this.xmodelVisible) {
            this.form.params = this.$refs.EXModelEditor.getDatas();
          }
          this.form.opType = this.opType;
          if (this.opType == 'UPDATE') {
            saveContent(this.form).then(response => {
              this.taskId = response.data.taskId;
              this.openProgress = true;
              this.progressTitle = "保存任务"
            });
          } else {
            this.form.catalogId = this.catalogId;
            this.form.contentType = this.contentType;
            addContent(this.form).then(response => {
              this.taskId = response.data.taskId;
              this.openProgress = true;
              this.progressTitle = "保存任务"
            });
          }
        }
      });
    },
    handlePublish () {
      publishContent([ this.form.contentId ]).then(response => {
          this.$modal.msgSuccess("发布成功");
      });
    },
    handleProgressClose (result) {
      if (result.status == 'SUCCESS') {
        if (this.opType == 'ADD') {
          this.opType = 'UPDATE';
          this.$router.push({ path: "/cms/content/editor", query: { type: this.contentType, catalogId: this.catalogId, id: this.contentId } });
        }
        this.initData();
      }
    },
    handlePreview () {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "content", dataId: this.form.contentId },
      });
      window.open(routeData.href, '_blank');
    },
    handleChangeLockState () {
      if (this.isLock) {
        unLockContent(this.form.contentId).then(response => {
          this.form.isLock = 'N';
          this.$modal.msgSuccess("解锁成功");
        });
      } else {
        lockContent(this.form.contentId).then(response => {
          this.form.isLock = 'Y';
          this.form.lockUser = response.data;
          this.$modal.msgSuccess("锁定成功");
        });
      }
    },
    handleCatalogChange() {
      this.openCatalogSelector = true;
      this.catalogSelectorFor = "change";
    },
    handleCatalogSelectorOk(catalogs) {
      if (this.catalogSelectorFor === 'change') {
        if (this.form.contentId && this.form.contentId != null) {
          // 编辑内容
          if (this.form.catalogId != catalogs[0].id) {
            const data = {
              contentIds: [ this.form.contentId ],
              catalogId: catalogs[0].id
            };
            moveContent(data).then(response => {
              if (response.code == 200) {
                this.$modal.msgSuccess("操作成功");
                this.form.catalogId = catalogs[0].id;
                this.form.catalogName = catalogs[0].name;
              }
            });
          }
        } else {
          // 新建内容
          this.form.catalogId = catalogs[0].id;
          this.form.catalogName = catalogs[0].name;
        }
      } else if(this.catalogSelectorFor === 'linkflag') {
        if (catalogs && catalogs.length > 0) {
          this.form.redirectUrl = catalogs[0].internalUrl;
        }
      }
      this.openCatalogSelector = false;
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleLinkTo(type) {
      if (type === 'content') {
        this.openContentSelector = true;
      } else if (type === 'catalog') {
        this.openCatalogSelector = true;
        this.catalogSelectorFor = 'linkflag';
      }
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.form.redirectUrl = contents[0].internalUrl;
        this.openContentSelector = false;
      } else {
        this.$modal.msgWarning("请先选择一条记录");
      }
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    }
  }
};
</script>

<style scoped>
.el-form {
    width: 100%;
}
.el-form-item {
  margin-bottom: 5px;  
}
.card-title {
  margin-bottom: 5px;
}
.card-title .el-card__body {
  padding-bottom: 10px;
}
.card-editor {
  margin-top: 10px;
  min-height: 700px;
}
.art-editor-container {
  margin-top: 10px;
  max-width: 1320px;
}
#toolbar-container {
  z-index: 101;
}
#editor-container {
  z-index: 100;
}
</style>