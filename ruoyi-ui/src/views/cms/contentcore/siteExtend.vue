<template>
  <div class="site-extend-container">
    <el-row class="mb8">
      <el-button plain
                  type="success"
                  icon="el-icon-edit"
                  size="mini"
                  :disabled="!this.siteId"
                  @click="handleSaveExtend">保存</el-button>
    </el-row>
    <el-form ref="form_extend"
              :model="form_extend"
              v-loading="loading"
              :disabled="!this.siteId"
              label-width="200px"
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
        <el-form-item label="标题查重"
                      prop="RepeatTitleCheck">
          <el-select v-model="form_extend.RepeatTitleCheck" filterable placeholder="请选择">
            <el-option
              v-for="item in repeatCheckOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="扩展模型"
                      prop="ExtendModel">
          <el-select 
            v-model="form_extend.ExtendModel" 
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
        </el-form-item>
        <el-form-item label="内容发布更新列表页数"
                      prop="MaxPageOnContentPublish">
          <el-input-number v-model="form_extend.MaxPageOnContentPublish" controls-position="right" :min="-1"></el-input-number>
        </el-form-item>
        <el-form-item label="允许编辑已发布内容"
                      prop="PublishedContentEdit">
          <el-switch
            v-model="form_extend.PublishedContentEdit"
            active-text="是"
            inactive-text="否"
            active-value="Y"
            inactive-value="N">
          </el-switch>
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
        <el-form-item 
          label="正文首图作为logo"
          prop="AutoArticleLogo">
          <el-switch
            v-model="form_extend.AutoArticleLogo"
            active-text="是"
            inactive-text="否"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>素材库</span>
        </div>
        <el-form-item label="存储策略"
                      prop="FileStorageType">
          <el-radio-group v-model="form_extend.FileStorageType" size="mini" @change="handleFileStorageTypeChange">
            <el-radio-button label="Local">本地</el-radio-button>
            <el-radio-button label="MinIO">Minio</el-radio-button>
            <el-radio-button label="AliyunOSS">阿里云OSS</el-radio-button>
            <el-radio-button label="TencentCOS">腾讯云COS</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="access key">
          <el-input v-model="form_extend.FileStorageArgs.accessKey"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="access secret">
          <el-input v-model="form_extend.FileStorageArgs.accessSecret"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="bucket">
          <el-input v-model="form_extend.FileStorageArgs.bucket"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="domain">
          <el-input v-model="form_extend.FileStorageArgs.domain"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="endpoint">
          <el-input v-model="form_extend.FileStorageArgs.endpoint"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="pipeline">
          <el-input v-model="form_extend.FileStorageArgs.pipeline"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>图片水印配置</span>
        </div>
        <el-form-item label="开启图片水印"
                      prop="ImageWatermark">
          <el-switch
            v-model="form_extend.ImageWatermark"
            active-text="是"
            inactive-text="否"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" label="水印图片">
          <cms-image-viewer v-model="form_extend.ImageWatermarkArgs.image"
                            :src="form_extend.ImageWatermarkArgs.imageSrc"
                            :site="siteId"
                            action="/cms/site/upload_watermarkimage"></cms-image-viewer>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" label="水印位置">
          <div class="watermarker_position">
            <el-radio-group v-model="form_extend.ImageWatermarkArgs.position" text-color="" size="mini">
              <el-radio-button label="TOP_LEFT">左上</el-radio-button>
              <el-radio-button label="TOP_CENTER">上</el-radio-button>
              <el-radio-button label="TOP_RIGHT">右上</el-radio-button>
              <el-radio-button label="CENTER_LEFT">左</el-radio-button>
              <el-radio-button label="CENTER">中</el-radio-button>
              <el-radio-button label="CENTER_RIGHT">右</el-radio-button>
              <el-radio-button label="BOTTOM_LEFT">左下</el-radio-button>
              <el-radio-button label="BOTTOM_CENTER">下</el-radio-button>
              <el-radio-button label="BOTTOM_RIGHT">右下</el-radio-button>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" label="不透明度">
          <el-input-number v-model="form_extend.ImageWatermarkArgs.opacity"
                           controls-position="right"  
                           :precision="1" 
                           :step="0.1" 
                           :min="0.1" 
                           :max="1"></el-input-number>
          <el-tooltip placement="right" style="margin-left:5px;">
            <div slot="content">
              数值越低透明度越高，默认1不透明
            </div>
            <i class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" label="占比">
          <el-input-number v-model="form_extend.ImageWatermarkArgs.ratio"
                           controls-position="right"  
                           :step="1" 
                           :min="1" 
                           :max="100"></el-input-number>
          <el-tooltip placement="right" style="margin-left:5px;">
            <div slot="content">
              水印占目标图片的比例，水印宽高至少20
            </div>
            <i class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>词汇配置</span>
        </div>
        <el-form-item 
          label="开启敏感词替换"
          prop="SensitiveWordEnable">
          <el-switch
            v-model="form_extend.SensitiveWordEnable"
            active-text="是"
            inactive-text="否"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          label="开启易错词替换"
          prop="ErrorProneWordEnable">
          <el-switch
            v-model="form_extend.ErrorProneWordEnable"
            active-text="是"
            inactive-text="否"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          label="热词分组"
          prop="HotWordGroups">
          <el-checkbox-group v-model="form_extend.HotWordGroups">
            <el-checkbox v-for="group in hotWordGroups" :label="group.code" :key="group.code">{{ group.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>
<script>
import { saveSiteExtends, getSiteExtends  } from "@/api/contentcore/site";
import { getHotWordGroupOptions } from "@/api/word/hotWord";
import { listXModel } from "@/api/contentcore/exmodel";
import CMSSimpleImageViewer from '@/views/cms/components/SimpleImageViewer';

export default {
  name: "CMSSiteExtend",
  components: {
    "cms-image-viewer": CMSSimpleImageViewer
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
      loading: false,
      siteId: this.site,
      repeatCheckOptions: [
        { label: "不校验", value: "0" },
        { label: "全站校验", value: "1" },
        { label: "栏目校验", value: "2" }
        ],
      exmodelOptions: [],
      form_extend: {
        FileStorageArgs: {},
        ImageWatermarkArgs: {}
      },
      hotWordGroups: []
    };
  },
  watch: {
    siteId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadSiteExtends();
      }
    },
  },
  created() {
    this.loadEXModelList();
    this.loadSiteExtends();
    this.loadHotWordGroups();
  },
  methods: {
    loadSiteExtends () {
      this.loading = true;
      const params = { siteId: this.siteId };
      getSiteExtends(params).then(response => {
        this.form_extend = response.data == null ? {} : response.data;
        if (this.form_extend.ImageWatermarkArgs.image !== '') {
          this.form_extend.ImageWatermarkArgs.imageSrc = this.form_extend.PreviewPrefix + this.form_extend.ImageWatermarkArgs.image;
        }
        if (this.form_extend.HotWordGroups && this.form_extend.HotWordGroups != '') {
          this.form_extend.HotWordGroups = JSON.parse(this.form_extend.HotWordGroups);
        } else {
          this.form_extend.HotWordGroups = [];
        }
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
    handleFileStorageTypeChange() {
      if (this.form_extend.FileStorageType === 'local') {
        // this.form_extend.FileStorageArgs = {};
      }
    },
    handleSaveExtend () {
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
          saveSiteExtends(this.siteId, data).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("保存成功");
            } else {
              this.$modal.msgError(response.msg);
            }
          });
        }
      });
    }
  }
};
</script>
<style>
.site-extend-container .el-form-item {
  margin-bottom: 12px;
}
.site-extend-container .el-card {
  margin-bottom: 10px;
}
.site-extend-container .el-input, 
.site-extend-container .el-input-number  {
  width: 301.5px;
}
.site-extend-container .el-upload-list {
  width: 300px;
}
.watermarker_position {
  width: 188px;
  border: 1px solid #a7a7a7;
  border-radius: 4px;
  padding: 3px;
}
.watermarker_position .el-radio-button {
  margin: 2px;
}
.watermarker_position .el-radio-group .el-radio-button .el-radio-button__inner, 
.watermarker_position .el-radio-button:first-child .el-radio-button__inner,
.watermarker_position .el-radio-button:last-child .el-radio-button__inner {
  width: 56px;
  border: 1px dashed #a7a7a7;
  border-radius: 0;
}
</style>