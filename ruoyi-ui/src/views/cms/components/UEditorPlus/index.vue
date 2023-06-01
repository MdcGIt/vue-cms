<template>
  <div class="ueditor">
    <vue-ueditor-wrap 
      v-model="contentHtml"
      :editor-id="editorId"
      :config="editorConfig"
      :editorDependencies="editorDependencies"
      @before-init="handleBeforeInit"
      @ready="handleReady"
      :style="styles" />
    <!-- 素材库 -->
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      :upload-limit="100" 
      :rtype="resourceType"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      :contentType="contentType"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
    <!-- 第三方视频 -->
    <cms-third-video :open.sync="openThirdVideoDialog" @ok="handleThirdVideoDialogOk"></cms-third-video>
  </div>
</template>

<script>
import VueUEditorWrap from '@/components/UEditorWrap';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
import CMSUeditorThirdVideo from "./thrid-video";

const UE_HOME = '/UEditorPlus/';

export default {
  name: "UEditorPlus",
  props: {
    /* 编辑器的内容 */
    value: {
      type: String,
      default: "",
    },
    /* 最小高度 */
    height: {
      type: Number,
      default: 500,
    },
    editorId: {
      type: String,
      default: "ueditor"
    }
  },
  components: {
    "vue-ueditor-wrap": VueUEditorWrap,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    "cms-resource-dialog": CMSResourceDialog,
    "cms-third-video": CMSUeditorThirdVideo
  },
  computed: {
    styles() {
      let style = {};
      if (this.height) {
        style.minHeight = `${this.height}px`;
      }
      return style;
    },
  },
  watch: {
    contentHtml (newV) {
      this.$emit('input', newV)
    },
    value (newV) {
      this.contentHtml = newV
    }
  },
  created() {
    this.init();
  },
  beforeDestroy() {
  },
  data() {
    return {
      contentHtml: "",
      editorDependencies: [
        'ueditor.config.js',
        'ueditor.all.js',
        'xiumi/xiumi-ue-dialog-v5.js',
        'xiumi/xiumi-ue-v5.css',
      ],
      editorConfig: {
        UEDITOR_HOME_URL: UE_HOME,
        serverUrl: undefined,
        catchRemoteImageEnable: false, // 自动下载远程图片
        maximumWords:10000, // 最大字数
        initialFrameHeight: 800, // 初始高度
        autoSaveEnable: false, // 自动保存
        enableDragUpload: false,
        enablePasteUpload: false,
        imagePopup: true,
        pageBreakTag: '__XY_UEDITOR_PAGE_BREAK__',
        iframeCssUrlsAddition: [
          UE_HOME + 'themes/placeholder.css'
        ],
        lang: this.$i18n.locale ? this.$i18n.locale.toLowerCase() : 'zh-cn',
        langPath: UE_HOME + "lang/",
        iframeUrlMap: {
          anchor: UE_HOME + "dialogs/anchor/anchor.html?20220503",
          link: UE_HOME + "dialogs/link/link.html?20220503",
          spechars: UE_HOME + "dialogs/spechars/spechars.html?20220503",
          searchreplace: UE_HOME + "dialogs/searchreplace/searchreplace.html?20220503",
          insertvideo: UE_HOME + "dialogs/video/video.html?20220503",
          help: UE_HOME + "dialogs/help/help.html?20220503",
          preview: UE_HOME + "dialogs/preview/preview.html?20220503",
          emotion: UE_HOME + "dialogs/emotion/emotion.html?20220503",
          wordimage: UE_HOME + "dialogs/wordimage/wordimage.html?20220902",
          formula: UE_HOME + "dialogs/formula/formula.html?20220902",
          attachment: UE_HOME + "dialogs/attachment/attachment.html?20220503",
          insertframe: UE_HOME + "dialogs/insertframe/insertframe.html?20220503",
          edittip: UE_HOME + "dialogs/table/edittip.html?20220503",
          edittable: UE_HOME + "dialogs/table/edittable.html?20220503",
          edittd: UE_HOME + "dialogs/table/edittd.html?20220503",
          scrawl: UE_HOME + "dialogs/scrawl/scrawl.html?20220503",
          template: UE_HOME + "dialogs/template/template.html?20220503",
          background: UE_HOME + "dialogs/background/background.html?20220503",
        },
        toolbars: [
          [
            "fullscreen",   // 全屏
            "source",       // 源代码
            "|",
            "undo",         // 撤销
            "redo",         // 重做
            "|",
            "bold",         // 加粗
            "italic",       // 斜体
            "underline",    // 下划线
            "fontborder",   // 字符边框
            "strikethrough",// 删除线
            "superscript",  // 上标
            "subscript",    // 下标
            "removeformat", // 清除格式
            "formatmatch",  // 格式刷
            "autotypeset",  // 自动排版
            "blockquote",   // 引用
            "pasteplain",   // 纯文本粘贴模式
            "|",
            "forecolor",    // 字体颜色
            "backcolor",    // 背景色
            "insertorderedlist",   // 有序列表
            "insertunorderedlist", // 无序列表
            "selectall",    // 全选
            "cleardoc",     // 清空文档
            "|",
            "rowspacingtop",// 段前距
            "rowspacingbottom",    // 段后距
            "lineheight",          // 行间距
            "|",
            "customstyle",         // 自定义标题
            "paragraph",           // 段落格式
            "fontfamily",          // 字体
            "fontsize",            // 字号
            "|",
            "directionalityltr",   // 从左向右输入
            "directionalityrtl",   // 从右向左输入
            "indent",              // 首行缩进
            "|",
            "justifyleft",         // 居左对齐
            "justifycenter",       // 居中对齐
            "justifyright",
            "justifyjustify",      // 两端对齐
            "|",
            "touppercase",         // 字母大写
            "tolowercase",         // 字母小写
            "|",
            "link",                // 超链接
            "unlink",              // 取消链接
            "anchor",              // 锚点
            "|",
            "imagenone",           // 图片默认
            "imageleft",           // 图片左浮动
            "imageright",          // 图片右浮动
            "imagecenter",         // 图片居中
            "|",
            "emotion",             // 表情
            // "scrawl",              // 涂鸦
            'xy-resource',
            'xy-content',
            "xy-third-video",         // 视频
            // "insertframe",         // 插入Iframe
            "insertcode",          // 插入代码
            "pagebreak",           // 分页
            "template",            // 模板
            "background",          // 背景
            "formula",             // 公式
            "|",
            "horizontal",          // 分隔线
            "date",                // 日期
            "time",                // 时间
            "spechars",            // 特殊字符
            "wordimage",           // Word图片转存
            "|",
            "inserttable",         // 插入表格
            "deletetable",         // 删除表格
            "insertparagraphbeforetable",     // 表格前插入行
            "insertrow",           // 前插入行
            "deleterow",           // 删除行
            "insertcol",           // 前插入列
            "deletecol",           // 删除列
            "mergecells",          // 合并多个单元格
            "mergeright",          // 右合并单元格
            "mergedown",           // 下合并单元格
            "splittocells",        // 完全拆分单元格
            "splittorows",         // 拆分成行
            "splittocols",         // 拆分成列
            "|",
            "searchreplace",       // 查询替换
            "xiumi-dialog",
          ],
        ]
      },
      openResourceDialog: false,
      resourceType: 'image',
      openThirdVideoDialog: false,
      openCatalogSelector: false,
      openContentSelector: false,
      contentType: ''
    };
  },
  methods: {
    init() {
    },
    handleBeforeInit(editorId) {
      console.log('ueditor-plus.before-init', editorId)
      this.addXyContentButton(editorId)
      this.addXyResourceButton(editorId)
      this.addThirdVideoButton(editorId)
    },
    handleReady(editorInstance) {
      console.log('ueditor-plus.ready: ' + editorInstance.key, editorInstance)
    },
    addXyContentButton(eidtorId) {
      const that = this
      window.UE.registerUI('xy-content', function (editor, uiName) {
        editor.registerCommand(uiName,{
          execCommand:function(cmdName,value){
            that.handleXyContentButtonClick(cmdName, value);
          }
        });
        const _onMenuClick = function() {
            editor.execCommand(uiName, this.value);
          }
        const items = [
          {
            label: "栏目链接",
            value: "catalog",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: "内容链接",
            value: "content",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: "组图",
            value: "img_group",
            theme: editor.options.theme,
            onclick: _onMenuClick
          }
        ];
        const ui = new UE.ui.MenuButton({
          editor: editor,
          className: "edui-for-" + uiName,
          title: "插入内容",
          items: items,
          onbuttonclick: function() {
            editor.execCommand(uiName, that.contentType);
          }
        });
        return ui;
      });
    },
    handleXyContentButtonClick(cmd, value) {
      if (value == 'catalog') {
        this.openCatalogSelector = true
      } else {
        if (value == 'img_group') {
          this.contentType = "image"
        }
        this.openContentSelector = true
      }
    },
    handleCatalogSelectorOk(catalogs) {
      if (catalogs && catalogs.length > 0) {
        console.log(catalogs)
        var editor = window.UE.getEditor(this.editorId)
        editor.execCommand("insertHTML", '<a href="' + catalogs[0].props.internalUrl + '">' + catalogs[0].name + '</a>');
        this.openCatalogSelector = false;
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false;
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        console.log(contents)
        var editor = window.UE.getEditor(this.editorId)
        if (this.contentType == 'image') {
          // 插入组图
          const html = '<p class="text-align:center;"><img src="/UEditorPlus/themes/default/images/spacer.gif" ex_cid="'
            + contents[0].contentId + '" title="' + contents[0].title + '" class="img_group_placeholder" /></p>'
          editor.execCommand("insertHTML", html);
        } else {
          editor.execCommand("insertHTML", '<a href="' + contents[0].internalUrl + '">' + contents[0].title + '</a>');
        }
        this.openContentSelector = false;
      } else {
        this.$modal.msgWarning(this.$t('Common.SelectFirst'));
      }
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
      this.contentType = ''
    },
    addXyResourceButton(editorId) {
      const that = this
      window.UE.registerUI('xy-resource', function (editor, uiName) {
        editor.registerCommand(uiName,{
          execCommand:function(cmdName,value){
            that.handleXyResourceButtonClick(cmdName, value);
          }
        });
        const _onMenuClick = function() {
            editor.execCommand(uiName, this.value);
          }
        const items = [
          {
            label: that.$t('CMS.UEditor.ResourceType.Image'),
            value: "image",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.ResourceType.Audio'),
            value: "audio",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.ResourceType.Video'),
            value: "video",
            theme: editor.options.theme,
            onclick: _onMenuClick
          },
          {
            label: that.$t('CMS.UEditor.ResourceType.File'),
            value: "file",
            theme: editor.options.theme,
            onclick: _onMenuClick
          }
        ];
        const ui = new UE.ui.MenuButton({
          editor: editor,
          className: "edui-for-" + uiName,
          title: that.$t('CMS.UEditor.InsertResource'),
          items: items,
          onbuttonclick: function() {
            editor.execCommand(uiName, that.resourceType);
          }
        });
        return ui;
      });
    },
    handleXyResourceButtonClick(cmd, value) {
      this.openResourceDialog = true
      this.resourceType = value
    },
    handleResourceDialogOk (results) {
      if (results && results.length > 0) {
        const r = results[0];
        var html;
        if (r.resourceType == 'image') {
          html = "<img src='" + r.src + "' iurl='" + r.path + "' class='art-body-img' />" 
        } else {
          html = '<a href="' + r.src + '" iurl="' + r.path + '" target="_blank">' + r.name + '</a>'
        }
        if (html && html.length > 0) {
          var editor = window.UE.getEditor(this.editorId)
          editor.execCommand("insertHTML",html);
        }
      }
    },
    // 插入第三方视频分享
    addThirdVideoButton(editorId) {
      const that = this
      window.UE.registerUI('xy-third-video', function (editor, uiName) {
        editor.registerCommand(uiName, {
          execCommand: function(cmdName){
            that.handleThirdVideoButtonClick(cmdName);
          }
        });
        const ui = new UE.ui.Button({
          className: "edui-for-" + uiName,
          name: that.$t('CMS.UEditor.InsertThirdVideo'),
          title: that.$t('CMS.UEditor.InsertThirdVideo'),
          onclick: function() {
            editor.execCommand(uiName);
          }
        });
        return ui;
      });
    },
    handleThirdVideoButtonClick(cmd, value) {
      this.openThirdVideoDialog = true
    },
    handleThirdVideoDialogOk (result) {
      if (result && result.length > 0) {
        var editor = window.UE.getEditor(this.editorId)
        editor.execCommand("insertHTML", result);
      }
    },
  },
};
</script>
<style>
.edui-for-xy-resource .edui-menubutton-body .edui-icon {
  background-image: url('./images/image.png') !important;
  /* background-size: contain; */
}
.edui-for-xy-third-video .edui-button-body .edui-icon {
  background-image: url('./images/video.png') !important;
}
.edui-for-xy-content .edui-menubutton-body .edui-icon {
  background-image: url('./images/content.png') !important;
}
</style>
