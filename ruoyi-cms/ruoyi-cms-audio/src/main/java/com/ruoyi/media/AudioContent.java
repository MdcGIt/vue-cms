package com.ruoyi.media;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.AbstractContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.media.domain.CmsAudio;
import com.ruoyi.media.service.IAudioService;

public class AudioContent extends AbstractContent<List<CmsAudio>> {

	private IAudioService audioService;

	@Override
	public String getContentType() {
		return AudioContentType.ID;
	}

	@Override
	public Long add() {
		super.add();
		this.getContentService().save(this.getContentEntity());

		List<CmsAudio> audioList = this.getExtendEntity();

		if (this.getContentEntity().isLinkContent()
				|| ContentCopyType.isMapping(this.getContentEntity().getCopyType())) {
			return this.getContentEntity().getContentId();
		}
		if (StringUtils.isNotEmpty(audioList)) {
			for (int i = 0; i < audioList.size(); i++) {
				CmsAudio audio = audioList.get(i);
				audio.setAudioId(IdUtils.getSnowflakeId());
				audio.setContentId(this.getContentEntity().getContentId());
				audio.setSiteId(this.getSiteId());
				audio.setType(FileExUtils.getExtension(audio.getPath()).toUpperCase());
				audio.setSortFlag(i);
				audio.createBy(this.getOperator().getUsername());
				this.getAudioService().progressAudioInfo(audio);
			}
			this.getAudioService().saveBatch(audioList);
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public Long save() {
		super.save();
		this.getContentService().updateById(this.getContentEntity());
		// 链接或映射内容直接删除所有音频数据
		if (this.getContentEntity().isLinkContent()
				|| ContentCopyType.isMapping(this.getContentEntity().getCopyType())) {
			this.getAudioService().remove(new LambdaQueryWrapper<CmsAudio>().eq(CmsAudio::getContentId,
					this.getContentEntity().getContentId()));
			return this.getContentEntity().getContentId();
		}
		// 音频数处理
		List<CmsAudio> audioList = this.getExtendEntity();
		// 先删除音频
		List<Long> deleteAudioIds = audioList.stream().filter(audio -> IdUtils.validate(audio.getAudioId()))
				.map(CmsAudio::getAudioId).toList();
		if (deleteAudioIds.size() > 0) {
			this.getAudioService()
					.remove(new LambdaQueryWrapper<CmsAudio>()
							.eq(CmsAudio::getContentId, this.getContentEntity().getContentId())
							.notIn(CmsAudio::getAudioId, deleteAudioIds));
		}
		// 查找需要修改的音频
		Map<Long, CmsAudio> oldAudioMap = this.getAudioService().lambdaQuery()
				.eq(CmsAudio::getContentId, this.getContentEntity().getContentId()).list().stream()
				.collect(Collectors.toMap(CmsAudio::getAudioId, a -> a));
		// 遍历请求音频列表，修改的音频数据path变更需重新设置音频属性
		for (int i = 0; i < audioList.size(); i++) {
			CmsAudio audio = audioList.get(i);
			if (IdUtils.validate(audio.getAudioId())) {
				CmsAudio dbAudio = oldAudioMap.get(audio.getAudioId());
				dbAudio.setTitle(audio.getTitle());
				dbAudio.setRemark(audio.getRemark());
				dbAudio.setAuthor(audio.getAuthor());
				dbAudio.setDescription(audio.getDescription());
				dbAudio.setSortFlag(i);
				if (dbAudio.getPath().equals(audio.getPath())) {
					dbAudio.setPath(audio.getPath());
					dbAudio.setType(FileExUtils.getExtension(audio.getPath()).toUpperCase());
					this.getAudioService().progressAudioInfo(dbAudio);
				}
				dbAudio.updateBy(this.getOperator().getUsername());
				this.getAudioService().updateById(dbAudio);
			} else {
				audio.setAudioId(IdUtils.getSnowflakeId());
				audio.setContentId(this.getContentEntity().getContentId());
				audio.setSiteId(this.getSiteId());
				audio.setType(FileExUtils.getExtension(audio.getPath()).toUpperCase());
				audio.setSortFlag(i);
				audio.createBy(this.getOperator().getUsername());
				this.getAudioService().progressAudioInfo(audio);
				this.getAudioService().save(audio);
			}
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public void delete() {
		super.delete();
		this.getAudioService().remove(
				new LambdaQueryWrapper<CmsAudio>().eq(CmsAudio::getContentId, this.getContentEntity().getContentId()));
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		super.copyTo(toCatalog, copyType);

		Long newContentId = (Long) this.getParams().get("NewContentId");
		List<CmsAudio> audioList = this.getAudioService().getAlbumAudioList(this.getContentEntity().getContentId());
		for (CmsAudio audio : audioList) {
			audio.createBy(this.getOperator().getUsername());
			audio.setAudioId(IdUtils.getSnowflakeId());
			audio.setContentId(newContentId);
			audio.setSiteId(toCatalog.getSiteId());
			this.getAudioService().save(audio);
		}
	}

	private IAudioService getAudioService() {
		if (this.audioService == null) {
			this.audioService = SpringUtils.getBean(IAudioService.class);
		}
		return this.audioService;
	}
}
