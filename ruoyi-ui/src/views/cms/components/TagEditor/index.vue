<template>
  <div class="tag-editor" style="padding: 0;">
    <el-tag :size="tagSize"
            closable
            :key="tag"
            v-for="tag in tagList"
            :disable-transitions="false"
            @close="handleRemoveTag(tag)">
            {{tag}}
    </el-tag>
    <el-input
      class="input-new-tag"
      v-if="inputVisible"
      ref="tagInput"
      :size="size"
      v-model="inputValue"
      @keyup.enter.native="handleInputConfirm"
      @blur="handleInputConfirm"
    >
    </el-input>
    <el-button v-else class="button-new-tag" icon="el-icon-plus" :size="size" @click="handleNewTag">{{ btnName }}</el-button>
  </div>
</template>
<script>
export default {
  name: "CMSTagEditor",
  model: {
    prop: 'tags',
    event: 'change'
  },
  props: {
    tags: {
      type: Array,
      default: [],
      required: true,
    },
    tagSize: {
      type: String,
      default: 'medium',
      required: false,
    },
    size: {
      type: String,
      default: 'mini',
      required: false,
    },
    btnName: {
      type: String,
      default: 'New',
      required: false,
    }
  },
  watch: {
    tags(newVal) {
      this.tagList = newVal;
    }
  },
  data () {
    return {
      tagList: this.tags,
      inputValue: '',
      inputVisible: false
    };
  },
  methods: {
    handleRemoveTag(tag) {
      this.tagList.splice(this.tagList.indexOf(tag), 1);
    },
    handleNewTag() {
      this.inputVisible = true;
      this.$nextTick(_ => {
        this.$refs.tagInput.$refs.input.focus();
      });
    },
    handleInputConfirm() {
      if (this.tagList.indexOf(this.inputValue) > -1) {
        this.$modal.msgWarning("Repeated!");
        return;
      }
      let inputValue = this.inputValue;
      if (inputValue) {
        this.tagList.push(inputValue.trim());
      }
      this.inputVisible = false;
      this.inputValue = '';
    }
  }
};
</script>
<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
.button-new-tag {
  margin-left: 10px;
}
.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>