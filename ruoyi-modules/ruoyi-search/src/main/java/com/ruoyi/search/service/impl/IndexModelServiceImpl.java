package com.ruoyi.search.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.search.core.ISearchType;
import com.ruoyi.search.domain.IndexModel;
import com.ruoyi.search.domain.IndexModelField;
import com.ruoyi.search.domain.dto.SearchModelDTO;
import com.ruoyi.search.domain.dto.SearchModelDTO.SearchIndexField;
import com.ruoyi.search.exception.SearchErrorCode;
import com.ruoyi.search.mapper.IndexModelFieldMapper;
import com.ruoyi.search.mapper.IndexModelMapper;
import com.ruoyi.search.service.IIndexModelService;
import com.ruoyi.system.fixed.dict.YesOrNo;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class IndexModelServiceImpl extends ServiceImpl<IndexModelMapper, IndexModel> implements IIndexModelService {

	private static final String CACHE_PREFIX = "search:model:";

	private final IndexModelFieldMapper modelFieldMapper;

	private final RedisCache redisCache;

	private final List<ISearchType> searchTypes;
	
	private final AsyncTaskManager asyncTaskManager;

	private ISearchType getSearchType(String type) {
		Optional<ISearchType> opt = this.searchTypes.stream().filter(t -> t.getType().equals(type)).findFirst();
		Assert.isTrue(opt.isPresent(), () -> SearchErrorCode.UNSUPPORTED_SEARCH_TYPE.exception(type));
		return opt.get();
	}

	@Override
	public SearchModelDTO getIndexModel(String modelCode) {
		SearchModelDTO si = this.redisCache.getCacheObject(CACHE_PREFIX + modelCode);
		if (Objects.isNull(si)) {
			IndexModel one = this.lambdaQuery().eq(IndexModel::getCode, modelCode).one();
			Assert.notNull(one, () -> SearchErrorCode.MODEL_NOT_EXISTS.exception(modelCode));

			List<IndexModelField> fields = this.modelFieldMapper.selectList(
					new LambdaQueryWrapper<IndexModelField>().eq(IndexModelField::getModelId, one.getModelId()));

			si = new SearchModelDTO();
			si.setLabel(one.getName());
			si.setName(one.getCode());
			for (IndexModelField f : fields) {
				si.getFields().add(new SearchIndexField(f.getFieldLabel(), f.getFieldName(), f.getFieldType(),
						YesOrNo.isYes(f.getPrimaryKey())));
			}
			this.redisCache.setCacheObject(CACHE_PREFIX + modelCode, si);
		}
		return si;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addIndexModel(SearchModelDTO dto) {
		IndexModel model = new IndexModel();
		model.setCode(dto.getName());
		boolean checkUnique = this.lambdaQuery().eq(IndexModel::getCode, model.getCode())
				.ne(IdUtils.validate(model.getModelId()), IndexModel::getModelId, model.getModelId()).count() == 0;
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("code=" + model.getCode()));

		model.setModelId(IdUtils.getSnowflakeId());
		model.createBy(dto.getOperator().getUsername());
		this.save(model);

		dto.getFields().forEach(f -> {
			IndexModelField mf = new IndexModelField();
			mf.setFieldName(f.getName());
			mf.setFieldLabel(f.getLabel());
			mf.setFieldType(f.getType());
			mf.setModelId(model.getModelId());
			mf.setPrimaryKey(f.isPrimary() ? YesOrNo.YES : YesOrNo.NO);
			mf.setWeight(f.getWeight());
			mf.setIndex(f.isIndex() ? YesOrNo.YES : YesOrNo.NO);
			mf.setAnalyzer(f.getAnalyzer());
			mf.createBy(dto.getOperator().getUsername());
			this.modelFieldMapper.insert(mf);
		});
		// 创建索引
		this.asyncTaskManager.execute(() -> {
			try {
				this.getSearchType(model.getType()).addIndex(dto);
			} catch (ElasticsearchException | IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteIndexModel(List<Long> modelIds) {
		List<IndexModel> models = this.listByIds(modelIds);
		for (IndexModel model : models) {
			this.modelFieldMapper.delete(
					new LambdaQueryWrapper<IndexModelField>().eq(IndexModelField::getModelId, model.getModelId()));
		}
		this.removeBatchByIds(modelIds);
		// 删除索引
		this.asyncTaskManager.execute(() -> {
			models.forEach(model -> {
				try {
					this.getSearchType(model.getType()).deleteIndex(model.getCode());
				} catch (ElasticsearchException | IOException e) {
					e.printStackTrace();
				}
			});
		});
	}
}