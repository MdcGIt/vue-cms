<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'RuoyiVueCMSGit',
  data() {
    return {
      url: 'https://gitee.com/liweiyi/RuoYi-Vue-CMS'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>