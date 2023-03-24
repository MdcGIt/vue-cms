<template>
  <div class="app-groovy-container">
    <el-form :model="form" ref="form" :rules="rules" size="small" label-position="top" label-width="140px">
      <el-form-item label="Groovy Script" prop="scriptText">
        <el-input
          type="textarea"
          :rows="10"
          v-model="form.scriptText"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-s-promotion" size="mini" @click="handleExecute">执行</el-button>
      </el-form-item>
    </el-form>

    <el-row>
      <div class="groovy_result">
      {{ resultString }}
      </div>
    </el-row>
  </div>
</template>
<script>
import { executeGroovySrcity } from "@/api/system/groovy";

export default {
  name: "SysGroovyScript",
  data() {
    return {
      loading: true,
      resultString: "",
      form: {
        scriptText: ""
      },
      // 表单校验
      rules: {
        scriptText: [
          { required: true, message: "Groovy Script不能为空", trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    handleExecute: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          executeGroovySrcity(this.form).then(response => {
            this.$modal.msgSuccess(this.$t('Common.Success'));
            this.resultString = response.data;
          });
        }
      });
    }
  }
};
</script>
<style>
.app-groovy-container {
  padding: 20px;
}
.app-groovy-container .groovy_result {
  font-size: 14px;
  line-height: 22px;
  color: #444;
  white-space: break-spaces;
}
</style>
