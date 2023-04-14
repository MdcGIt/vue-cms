<template>
  <div class="app-container" v-loading="loading">
    <el-container>
      <el-header height="40px">
        <el-button 
          plain
          type="info"
          icon="el-icon-back"
          size="mini"
          @click="handleGoBack">返回上一页</el-button>
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          @click="handleSave">{{ $t("Common.Save") }}</el-button>
      </el-header>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="80px">
        <el-container>
          <el-aside style="width:500px;">
            <el-card shadow="never">
              <div slot="header" class="clearfix">
                <span>基础属性</span>
              </div>
              <el-form-item label="名称"
                            prop="name">
                <el-input v-model="form.name" />
              </el-form-item>
              <el-form-item label="类型"
                            prop="type">
                <el-select v-model="form.type">
                  <el-option
                    v-for="t in adTypes"
                    :key="t.id"
                    :label="t.name"
                    :value="t.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="权重"
                            prop="weight">
                <el-input-number v-model="form.weight" :min="0"></el-input-number>
              </el-form-item>
              <el-form-item label="上线时间"
                            prop="onlineDate">
                <el-date-picker v-model="form.onlineDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" />
              </el-form-item>
              <el-form-item label="下线时间"
                            prop="offlineDate">
                <el-date-picker v-model="form.offlineDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" />
              </el-form-item>
              <el-form-item :label="$t('Common.Remark')"
                            prop="remark">
                <el-input v-model="form.remark"
                          type="textarea"
                          maxlength="100" />
              </el-form-item>
            </el-card>
          </el-aside>
          <el-main>
            <el-card shadow="never">
              <div slot="header" class="clearfix; line-height: 32px; font-size: 16px;">
                <span>广告素材</span>
              </div>
              <el-form-item label=""
                v-if="form.type==='image'"
                prop="resourcePath">
                <cms-logo-view v-model="form.resourcePath" :src="form.resourceSrc"
                                :width="218" :height="150"></cms-logo-view>
              </el-form-item>
              <el-form-item
                label="跳转链接"
                prop="redirectUrl">
                <el-input v-model="form.redirectUrl" placeholder="http(s)://" />
              </el-form-item>
            </el-card>
          </el-main>
        </el-container>
      </el-form>
    </el-container>
  </div>
</template>
<style scoped>
.el-input, .el-textarea, .el-select, .el-input-number {
  width: 300px;
}
.el-header {
  padding-left: 0;
}
.el-aside, .el-main {
  padding: 0;
}
.el-aside {
  padding-right: 10px;
  background-color: #fff;
}
.el-aside, .el-container {
  line-height: 24px;
  font-size: 16px;
}
.el-card {
  height: 460px;
}
</style>
<script>
import { urlValidator } from '@/utils/validate'
import { listAdvertisementTypes, getAdvertisement, addAdvertisement, editAdvertisement } from "@/api/advertisement/advertisement";
import CMSLogoView from '@/views/cms/components/LogoView';

export default {
  name: "CMSAdvertisement",
  components: {
    'cms-logo-view': CMSLogoView
  },
  data () {
    return {
      loading: false,
      advertisementId: this.$route.query.id,
      adSpaceId: this.$route.query.adSpaceId,
      adTypes: [],
      form: {
        weight: 100
      },
      rules: {
        type: [
          { required: true, message: "类型不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        weight: [
          { required: true, message: "权重不能为空", trigger: "blur" },
        ],
        onlineDate: [
          { required: true, message: "上线时间不能为空", trigger: "blur" }
        ],
        offlineDate: [
          { required: true, message: "下线时间不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.loadAdvertisementTypes();
    if (this.advertisementId) {
      this.$modal.loading();
      getAdvertisement(this.advertisementId).then(response => {
        if (response.code == 200) {
          this.form = response.data;
        } else {
          this.advertisementId = undefined;
        }
        this.$modal.closeLoading();
      });
    }
  },
  methods: {
    loadAdvertisementTypes () {
      listAdvertisementTypes(this.queryParams).then(response => {
        if (response.code == 200) {
          this.adTypes = response.data.rows;
          if (!this.advertisementId && this.adTypes.length > 0) {
            this.form.type = this.adTypes[0].id;
          }
        }
      });
    },
    handleGoBack() {
      const obj = { path: "/cms/adspace/editor", query: { id: this.adSpaceId } };
      this.$tab.closeOpenPage(obj);
    },
    handleSave() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.form.advertisementId) {
            editAdvertisement(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess(response.msg);
              }
              this.loading = false;
            });
          } else {
            this.form.adSpaceId = this.adSpaceId;
            addAdvertisement(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess(response.msg);
                this.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: this.adSpaceId, id: response.data } });
              }
              this.loading = false;
            });
          }
        }
      });
    }
  }
};
</script>