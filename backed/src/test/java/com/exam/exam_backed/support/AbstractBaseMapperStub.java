package com.exam.exam_backed.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.session.ResultHandler;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseMapperStub<T> implements BaseMapper<T> {
    @Override
    public int deleteById(T entity) {
        throw unsupported();
    }

    @Override
    public int delete(Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public int updateById(T entity) {
        throw unsupported();
    }

    @Override
    public int update(T entity, Wrapper<T> updateWrapper) {
        throw unsupported();
    }

    @Override
    public T selectById(Serializable id) {
        throw unsupported();
    }

    @Override
    public List<T> selectByIds(Collection<? extends Serializable> idList) {
        throw unsupported();
    }

    @Override
    public void selectByIds(Collection<? extends Serializable> idList, ResultHandler<T> resultHandler) {
        throw unsupported();
    }

    @Override
    public Long selectCount(Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public List<T> selectList(Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public void selectList(Wrapper<T> queryWrapper, ResultHandler<T> resultHandler) {
        throw unsupported();
    }

    @Override
    public List<T> selectList(IPage<T> page, Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public void selectList(IPage<T> page, Wrapper<T> queryWrapper, ResultHandler<T> resultHandler) {
        throw unsupported();
    }

    @Override
    public List<Map<String, Object>> selectMaps(Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public void selectMaps(Wrapper<T> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
        throw unsupported();
    }

    @Override
    public List<Map<String, Object>> selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public void selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<T> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
        throw unsupported();
    }

    @Override
    public <E> List<E> selectObjs(Wrapper<T> queryWrapper) {
        throw unsupported();
    }

    @Override
    public <E> void selectObjs(Wrapper<T> queryWrapper, ResultHandler<E> resultHandler) {
        throw unsupported();
    }

    protected UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not needed by this unit test");
    }
}
