package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.FileAddDTO;
import com.ruoyi.contentcore.domain.dto.FileOperateDTO;
import com.ruoyi.contentcore.service.IFileService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.SaAdminCheckLogin;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 文件管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/file")
public class FileController extends BaseRestController {

	private final ISiteService siteService;

	private final IFileService fileService;

	/**
	 * 查询文件列表
	 * 
	 * @param filePath
	 * @param filename
	 * @return
	 */
	@GetMapping("/list")
	public R<?> getFileList(@RequestParam("filePath") @NotEmpty String filePath,
			@RequestParam("fileName") String filename) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return this.fileService.getSiteFileList(site, filePath, filename);
	}

	/**
	 * 获取目录树数据
	 * 
	 * @return
	 */
	@GetMapping("/directoryTreeData")
	public R<?> getDirectoryTree() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<TreeNode<String>> list = this.fileService.getSiteDirectoryTreeData(site);
		return R.ok(list);
	}

	/**
	 * 重命名文件
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/rename")
	public R<?> renameFile(@RequestBody FileOperateDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.renameFile(site, dto.getFilePath(), dto.getRename());
		return R.ok();
	}

	/**
	 * 新建文件
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/add")
	public R<?> addFile(@RequestBody FileAddDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.addFile(site, dto);
		return R.ok();
	}

	/**
	 * 上传文件
	 * 
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/upload")
	public R<?> uploadFile(@RequestParam("dir") @NotEmpty String dir, @RequestParam("file") MultipartFile multipartFile)
			throws IOException {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.uploadFile(site, dir, multipartFile);
		return R.ok();
	}

	/**
	 * 读取文件内容
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/read")
	public R<?> readFile(@RequestBody FileOperateDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return R.ok(this.fileService.readFile(site, dto.getFilePath()));
	}

	/**
	 * 修改文件内容
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/edit")
	public R<?> editFile(@RequestBody FileOperateDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.editFile(site, dto.getFilePath(), dto.getFileContent());
		return R.ok();
	}

	/**
	 * 删除文件
	 * 
	 * @param dtoList
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/delete")
	public R<?> deleteFile(@RequestBody @NotEmpty List<FileOperateDTO> dtoList) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String[] filePathArr = dtoList.stream().map(FileOperateDTO::getFilePath).toArray(String[]::new);
		this.fileService.deleteFiles(site, filePathArr);
		return R.ok();
	}
}
