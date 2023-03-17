<template>
  <div id="ryCkeditor5">
    <ckeditor 
      :editor="editor" 
      v-model="editorContent" 
      :config="editorConfig"
      @ready="onEditorReady"
      @focus="onEditorFocus"
      @blur="onEditorBlur"
      @input="onEditorInput"
      @destroy="onEditorDestroy"
    ></ckeditor>
    <el-button id="openResourceDialog" size="small" type="primary" style="display:none" @click="handleOpenResourceDialog"></el-button>
  </div>
</template>

<script>
import {getToken} from '@/utils/auth'
// CKEditor5 富文本编辑器
import CKEditor from '@ckeditor/ckeditor5-vue2';
import ClassicEditor from '@ckeditor/ckeditor5-editor-classic/src/classiceditor';
// import '@ckeditor/ckeditor5-build-classic/build/translations/zh-cn'
import axios from 'axios'
import FileRpository from "@ckeditor/ckeditor5-upload/src/filerepository";
// 必须配置Essentials，否则编辑器无法输入
import Essentials from "@ckeditor/ckeditor5-essentials/src/essentials";
import UploadAdapter from "@ckeditor/ckeditor5-adapter-ckfinder/src/uploadadapter";
import SimpleUploadAdapter from '@ckeditor/ckeditor5-upload/src/adapters/simpleuploadadapter';
import Alignment from "@ckeditor/ckeditor5-alignment/src/alignment";
import Autoformat from "@ckeditor/ckeditor5-autoformat/src/autoformat"
import Bold from "@ckeditor/ckeditor5-basic-styles/src/bold"
import Italic from "@ckeditor/ckeditor5-basic-styles/src/italic";
import Underline from "@ckeditor/ckeditor5-basic-styles/src/underline";
import Strikethrough from "@ckeditor/ckeditor5-basic-styles/src/strikethrough";
import Code from "@ckeditor/ckeditor5-basic-styles/src/code";
import CodeBlock from "@ckeditor/ckeditor5-code-block/src/codeblock";
import Font from "@ckeditor/ckeditor5-font/src/font";
import Subscript from "@ckeditor/ckeditor5-basic-styles/src/subscript";
import Superscript from "@ckeditor/ckeditor5-basic-styles/src/superscript";
import Highlight from "@ckeditor/ckeditor5-highlight/src/highlight";
import Heading from "@ckeditor/ckeditor5-heading/src/heading";
import HorizontalLine from "@ckeditor/ckeditor5-horizontal-line/src/horizontalline";
import Image from "@ckeditor/ckeditor5-image/src/image";
import ImageCaption from "@ckeditor/ckeditor5-image/src/imagecaption";
import ImageStyle from "@ckeditor/ckeditor5-image/src/imagestyle";
import ImageToolbar from "@ckeditor/ckeditor5-image/src/imagetoolbar";
import ImageUpload from "@ckeditor/ckeditor5-image/src/imageupload";
import ImageResize from "@ckeditor/ckeditor5-image/src/imageresize";
import Indent from "@ckeditor/ckeditor5-indent/src/indent";
import Link from "@ckeditor/ckeditor5-link/src/link";
import IndentBlock from "@ckeditor/ckeditor5-indent/src/indentblock";
import List from "@ckeditor/ckeditor5-list/src/list";
import MediaEmbed from "@ckeditor/ckeditor5-media-embed/src/mediaembed";
import Paragraph from "@ckeditor/ckeditor5-paragraph/src/paragraph";
import PasteFromOffice from "@ckeditor/ckeditor5-paste-from-office/src/pastefromoffice";
import Table from '@ckeditor/ckeditor5-table/src/table';
import TableToolbar from '@ckeditor/ckeditor5-table/src/tabletoolbar';
import TableProperties from '@ckeditor/ckeditor5-table/src/tableproperties';
import TableCellProperties from '@ckeditor/ckeditor5-table/src/tablecellproperties';
import TextTransformation from "@ckeditor/ckeditor5-typing/src/texttransformation";
import BlockToolbar from "@ckeditor/ckeditor5-ui/src/toolbar/block/blocktoolbar";
import HeadingButtonsUI from "@ckeditor/ckeditor5-heading/src/headingbuttonsui";
import ParagraphButtonUI from "@ckeditor/ckeditor5-paragraph/src/paragraphbuttonui";
// import browseFilesIcon from "@/assets/ckeditor5-upload/theme/icons/browse-files.svg";
// import browseFilesIcon from "@/assets/ckeditorIcon/browse-files.svg";
import { Plugin, icons } from '@ckeditor/ckeditor5-core/src/plugin';
import ButtonView from '@ckeditor/ckeditor5-ui/src/button/buttonview';

class MyUploadAdapter {
  constructor(loader,filename,remark) {
    // The file loader instance to use during the upload.
    this.loader = loader;
    this.filename = filename;
    this.remark = remark;
  }
 
  async upload() {
    const data = new FormData();
    data.append("file", await this.loader.file);
    data.append('name', this.filename);
    data.append('remark', this.filename);
    const res = await axios({
      url: process.env.VUE_APP_BASE_API + `/cms/resource`,
      method: "POST",
      headers: {
        Authorization: 'Bearer ' + getToken(),
        'content-type': 'application/x-www-form-urlencoded'
      },
      data,
      withCredentials: true, // true 为不允许带 token, false 为允许
    });
    return {
      default: res.data.data.fileUrl,
    };
  }
 
 
  // Aborts the upload process.
  abort() {
    // Reject the promise returned from the upload() method.
    server.abortUpload();
  }
}
 
class InsertResource extends Plugin {
  init() {
    const editor = this.editor;
 
    editor.ui.componentFactory.add( 'insertResource', locale => {
      const view = new ButtonView( locale );
 
      view.set( {
        label: '插入资源文件',
        icon: icons.browse-files,
        tooltip: true,
      } );
 
      // Callback executed once the image is clicked.
      view.on( 'execute', () => {
        $("#openResourceDialog").click();
      } );
      return view;
    } );
  }
}

export default {
  name: 'RyCKEditor5',
  components: {
    "ckeditor": CKEditor.component
  },
  props: {
    /* 编辑器的内容 */
    value: {
      type: String,
      default: "",
    },
    // 图片名称
    filename: {
      type: String,
      default: "",
    },
    // 备注
    remark: {
      type: String,
      default: "",
    },
    /* 编辑器最小高度 */
    minHeight: {
      type: Number,
      default: 200,
    },
  },
  data() {
    return {
      myEditor:null,
      // CKEditor
      editor: ClassicEditor,
      editorContent: this.value, //v-model初始化值
      editorConfig: {
        language: 'zh-cn',  //简体中文显示
        placeholder: '请输入...', //默认填充内容
        plugins: [
          Alignment,
          Highlight,
          Essentials,
          UploadAdapter,
          Autoformat,
          Bold,
          Italic,
          Underline,
          Strikethrough,
          SimpleUploadAdapter,
          Code,
          CodeBlock,
          Font,
          Subscript,
          Superscript,
          Heading,
          HorizontalLine,
          InsertResource,
          Image,
          ImageCaption,
          ImageStyle,
          ImageToolbar,
          ImageUpload,
          ImageResize,
          Indent,
          IndentBlock,
          Link,
          List,
          MediaEmbed,
          Paragraph,
          PasteFromOffice,
          FileRpository,
          Table,
          TableToolbar,
          TableProperties,
          TableCellProperties,
          TextTransformation,
          BlockToolbar,
          ParagraphButtonUI,
          HeadingButtonsUI
        ],
        //功能模块
        toolbar: [
            "undo",
            "redo",
            "|",
            "heading",
            "Highlight",
            "fontSize",
            "fontFamily",
            "fontColor",
            "fontBackgroundColor",
            "|",
            "imageUpload",
            "insertResource",
            "insertTable",
            "|",
            "bold",
            "italic",
            "underline",
            "strikethrough",
            "code",
            "subscript",
            "superscript",
            "link",
            "bulletedList",
            "numberedList",
            "alignment",
            "horizontalLine",
            "indent",
            "outdent",
            "mediaEmbed",
        ],
        fontSize: {
          options: [ 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36 ]
        },
        fontFamily: {
          options: [ 
            'default',
            'Blackoak Std',
            '宋体,SimSun',
            '新宋体,NSimSun',
            '黑体,SimHei',
            '微软雅黑,Microsoft YaHei',
            '楷体_GB2312,KaiTi_GB2312',
            '隶书,LiSu',
            '幼园,YouYuan',
            '华文细黑,STXihei',
            '细明体,MingLiU',
            '新细明体,PMingLiU'
          ]
        },
        //表格配置
        table: {
          contentToolbar: [
            'tableColumn', 'tableRow', 'mergeTableCells', 'tableCellProperties', 'tableProperties'
          ],
          tableProperties: {
            // The default styles for tables in the editor.
            // They should be synchronized with the content styles.
            defaultProperties: {
              borderStyle: 'solid',
              borderColor: '#666',
              borderWidth: '1px',
              alignment: 'left',
              width: '550px',
              height: '100px'
            },
            // The default styles for table cells in the editor.
            // They should be synchronized with the content styles.
            tableCellProperties: {
              defaultProperties: {
                horizontalAlignment: 'center',
                verticalAlignment: 'bottom',
                padding: '10px',
                borderStyle: 'solid',
                borderColor: '#666',
                borderWidth: '1px',
              }
            }
          }
        },
        simpleUpload: {
          uploadUrl: process.env.VUE_APP_BASE_API + `/cms/resource/upload`, //上传图片的接口
          withCredentials: true,
          headers: {
            Authorization: 'Bearer ' + getToken()
          },
          data:{
            uploadParams: {
              bizType: "DoctorRecruit",
            },
          },
        },
        // 允许img等标签存在iurl属性
        htmlSupport: {
          allow: [
            {
              name: /^(img|a|video|audio|source)$/,
              attributes: {
                  iurl: true
              }
            }
          ]
        },
        // 第三方视频引用支持
        mediaEmbed: {
          previewsInData: true,
          providers: [
            {
              name: 'youku',
              url: [
                // https://v.youku.com/v_show/id_XNTk0NjI0NjExMg==.html?spm=a2hja.14919748_WEBHOME_NEW.drawer3.d_zj1_1&scm=20140719.rcmd.37023.video_XNTk0NjI0NjExMg==
                // <iframe height=498 width=510 src='https://player.youku.com/embed/XNTk0NjI0NjExMg==' frameborder=0 'allowfullscreen'></iframe>
                /^https:\/\/v\.youku\.com\/v_show\/id_([\w=]+)\.html/,
                /^<iframe[^>]+src=['"](https:\/\/player\.youku\.com\/[^'"]+)['"].*?<\/iframe>/,
              ],
              html: match => {
                const playerUrl = match[1].startsWith('https://') ? match[1] : "https://player.youku.com/embed/" + match[1];
                console.log('palyerUrl.youku', playerUrl);
                return  '<div style="position: relative;text-align:center;">' + 
                          `<iframe width="100%" height="510" src="${playerUrl}" frameborder="0" allowfullscreen></iframe>` +
                        '</div>';
              }
            },
            {
              name: 'qq',
              url: [
                // https://v.qq.com/x/cover/mzc00200cgo4wcc/r00454x3b5p.html
                // <iframe frameborder="0" src="https://v.qq.com/txp/iframe/player.html?vid=r00454x3b5p" allowFullScreen="true"></iframe>
                // https://v.qq.com/x/page/w350438y1fs.html
                // <iframe frameborder="0" src="https://v.qq.com/txp/iframe/player.html?vid=w350438y1fs" allowFullScreen="true"></iframe>
                /^https:\/\/v\.qq\.com\/x\/cover\/[\w]+\/(\w+)\.html/,
                /^https:\/\/v\.qq\.com\/x\/page\/(\w+)\.html/,
                /^<iframe[^>]+src=['"](https:\/\/v\.qq\.com\/[^'"]+)['"].*?<\/iframe>/,
              ],
              html: match => {
                const playerUrl = match[1].startsWith('https://') ? match[1] : "https://v.qq.com/txp/iframe/player.html?vid=" + match[1];
                console.log('palyerUrl.qq', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}" frameborder="0" allowfullscreen="true"></iframe>` +
                        '</div>';
              }
            },
            {
              name: 'iqiyi',
              url: [
                // http://www.iqiyi.com/v_19rxkql7qk.html
                // http://www.iqiyi.com/a_1dwtgvklkr5.html
                /^http:\/\/www\.iqiyi\.com\/[\w]_(\w+)\.html/,
              ],
              html: match => {
                const playerUrl = match[0];
                console.log('palyerUrl', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}" frameborder="0" allowfullscreen="true"></iframe>` +
                        '</div>';
              }
            },
            {
              name: 'bilibili',
              url: [
                // <iframe src="//player.bilibili.com/player.html?aid=694195827&bvid=BV1r24y1W7E1&cid=1003788458&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>
                // <iframe src="//player.bilibili.com/player.html?aid=947755350&bvid=BV1YW4y137Kk&cid=966243153&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>
                /^<iframe[^>]+src=['"](\/\/player\.bilibili\.com\/[^'"]+)['"].*?<\/iframe>/,
              ],
              html: match => {
                const playerUrl = match[1];
                console.log('palyerUrl', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}"` + 
                          ' scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"></iframe>' +
                        '</div>';
              }
            }, 
            {
              name: 'haokan',
              url: [
                // https://haokan.baidu.com/v?vid=17491163422580795321&pd=pcshare&hkRelaunch=p1%3Dpc%26p2%3Dvideoland%26p3%3Dshare_input
                /^https:\/\/haokan\.baidu\.com\/v\?vid=.*?share_input/,
              ],
              html: match => {
                const playerUrl = match[0];
                console.log('palyerUrl', playerUrl);
                return  '<div style="position: relative;text-align:center;">' +
                          `<iframe width="100%" height="510" src="${playerUrl}"` + 
                          ' scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"></iframe>' +
                        '</div>';
              }
            }
          ]
        }
      }
    };
  },
  watch: {
    value: function () {
      this.editorContent = this.value;
    },
    editorContent: function () {
      this.$emit("input", this.editorContent);
    }
  },
  methods: {
    handleOpenResourceDialog() {
      console.log("handleOpenResourceDialog", "nice~");
    },
    onEditorReady( editor )  {
      console.log("CKEditor ready...");
      // 设置下编辑器最小高度
      editor.editing.view.change(writer => {
        writer.setStyle('min-height', '800px', editor.editing.view.document.getRoot());
      });
       // 自定义上传图片插件
      editor.plugins.get("FileRepository").createUploadAdapter = loader => {
        return new MyUploadAdapter(loader, this.filename, this.remark);
      };
      this.myEditor = editor
    },
    onEditorFocus() {
      console.log("CKEditor focus...");
    },
    onEditorBlur() {
      console.log("CKEditor blur...");
    },
    onEditorInput() {
      console.log("CKEditor input...");
    },
    onEditorDestroy() {
      console.log("CKEditor destory...");
    }
  }
}
</script>