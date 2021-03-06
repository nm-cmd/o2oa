package com.x.okr.assemble.control.jaxrs.okrconfigsystem;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.okr.assemble.control.jaxrs.okrconfigsystem.exception.ExceptionSystemConfigCodeEmpty;
import com.x.okr.assemble.control.jaxrs.okrconfigsystem.exception.ExceptionSystemConfigNotExists;
import com.x.okr.assemble.control.jaxrs.okrconfigsystem.exception.ExceptionSystemConfigQueryByCode;
import com.x.okr.assemble.control.jaxrs.okrconfigsystem.exception.ExceptionWrapInConvert;
import com.x.okr.entity.OkrConfigSystem;

import net.sf.ehcache.Element;

public class ActionGetByCode extends BaseAction {

	private static  Logger logger = LoggerFactory.getLogger( ActionGetByCode.class );
	
	protected ActionResult<Wo> execute( HttpServletRequest request,EffectivePerson effectivePerson, JsonElement jsonElement ) throws Exception {
		ActionResult<Wo> result = new ActionResult<Wo>();
		Wo wrap = null;
		Wi wrapIn = null;
		Boolean check = true;
		OkrConfigSystem okrConfigSystem = null;
		
		try {
			wrapIn = this.convertToWrapIn( jsonElement, Wi.class );
		} catch (Exception e ) {
			check = false;
			Exception exception = new ExceptionWrapInConvert( e, jsonElement );
			result.error( exception );
			logger.error( e, effectivePerson, request, null);
		}
		
		if( check ) {
			if( wrapIn.getConfigCode() == null || wrapIn.getConfigCode().isEmpty() ){
				Exception exception = new ExceptionSystemConfigCodeEmpty();
				result.error( exception );
			}else{
				String cacheKey = catchNamePrefix + "." + wrapIn.getConfigCode();
				Element element = null;			
				element = cache.get( cacheKey );
				if( element != null ){
					wrap = ( Wo ) element.getObjectValue();
					result.setData( wrap );
				}else{
					try {
						okrConfigSystem = okrConfigSystemService.getWithConfigCode( wrapIn.getConfigCode() );
						if( okrConfigSystem != null ){
							wrap = Wo.copier.copy( okrConfigSystem );						
							cache.put( new Element( cacheKey, wrap ) );						
							result.setData(wrap);
						}else{
							Exception exception = new ExceptionSystemConfigNotExists( wrapIn.getConfigCode() );
							result.error( exception );
						}
					} catch (Exception e) {
						Exception exception = new ExceptionSystemConfigQueryByCode( e, wrapIn.getConfigCode() );
						result.error( exception );
						logger.error( e, effectivePerson, request, null);
					}
				}
			}
		}
		return result;
	}
	
	public static class Wi extends OkrConfigSystem {

		private static final long serialVersionUID = -5076990764713538973L;

		public static List<String> Excludes = new ArrayList<String>(JpaObject.FieldsUnmodify);

	}
	
	public static class Wo extends OkrConfigSystem{

		private static final long serialVersionUID = -5076990764713538973L;

		public static List<String> Excludes = new ArrayList<String>();

		public static WrapCopier<OkrConfigSystem, Wo> copier = WrapCopierFactory.wo( OkrConfigSystem.class, Wo.class, null, JpaObject.FieldsInvisible);
		
		private Long rank = 0L;

		public Long getRank() {
			return rank;
		}

		public void setRank(Long rank) {
			this.rank = rank;
		}
	}
}