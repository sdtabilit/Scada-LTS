/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.view.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataSource.meta.ScriptExecutor;
import com.serotonin.mango.view.ImplDefinition;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.web.dwr.BaseDwr;
import com.serotonin.mango.web.taglib.Functions;
import com.serotonin.util.SerializationHelper;
import org.mozilla.javascript.Undefined;

import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorExecutionScript;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class ScriptComponent extends PointComponent {
	public static ImplDefinition DEFINITION = new ImplDefinition("script", "SCRIPT", "graphic.script", new int[] {
			DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC, DataTypes.ALPHANUMERIC });

	private static final String SCRIPT_PREFIX = "function __scriptRenderer__() {";
	private static final String SCRIPT_SUFFIX = "\r\n}\r\n__scriptRenderer__();";

	@JsonRemoteProperty
	private String script;

	private static final Log LOG = LogFactory.getLog(ScriptComponent.class);

	@Override
	public String snippetName() {
		return "scriptContent";
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public void addDataToModel(Map<String, Object> model, PointValueTime value) {
		String result;

		if (value == null)
			result = "--";
		else {
			// Create the script engine.
			// ScriptEngineManager manager;
			Context cx = Context.enter();
			cx.setOptimizationLevel(Common.getEnvironmentProfile().getInt("js.optimizationlevel", 0));

			/*
			 * try { manager = new ScriptEngineManager(); } catch (Exception e)
			 * { throw new ScriptException(e); }
			 */
			try {
				Scriptable scope = cx.initStandardObjects();
				// ScriptEngine engine = manager.getEngineByName("JavaScript");
				// engine.getContext().setErrorWriter(new
				// PrintWriter(System.err));
				// engine.getContext().setWriter(new PrintWriter(System.out));

				DataPointVO point = tgetDataPoint();

				// Put the values into the engine scope.
				scope.put("value", scope, value.getValue().getObjectValue());
				scope.put("htmlText", scope, Functions.getHtmlText(point, value));
				scope.put("renderedText", scope, Functions.getRenderedText(point, value));
				scope.put("time", scope, value.getTime());
				scope.put("pointComponent", scope, this);
				scope.put("point", scope, point);
				// Copy properties from the model into the engine scope.
				scope.put(BaseDwr.MODEL_ATTR_EVENTS, scope, model.get(BaseDwr.MODEL_ATTR_EVENTS));
				scope.put(BaseDwr.MODEL_ATTR_HAS_UNACKED_EVENT, scope, model.get(BaseDwr.MODEL_ATTR_HAS_UNACKED_EVENT));
				scope.put(BaseDwr.MODEL_ATTR_RESOURCE_BUNDLE, scope, model.get(BaseDwr.MODEL_ATTR_RESOURCE_BUNDLE));

				// Create the script.
				String evalScript = SCRIPT_PREFIX + script + SCRIPT_SUFFIX;

				// Execute.
				try {
					// Object o = engine.eval(evalScript);
					Object o = cx.evaluateString(scope, evalScript, "<cmd>", 1, null);
					if (o == null)
						result = null;
					else
						result = o.toString();

					if(o instanceof Undefined) {
						LOG.error(infoErrorExecutionScript(model, this));
					}
				} catch (Exception e) {
					result = ScriptExecutor.prettyScriptMessage(new ScriptException(e)).getMessage();
					LOG.error(infoErrorExecutionScript(e, model, this));
				}
			} finally {
				Context.exit();
			}

		}

		model.put("scriptContent", result);
	}

	@Override
	public ImplDefinition definition() {
		return DEFINITION;
	}

	//
	// /
	// / Serialization
	// /
	//
	private static final long serialVersionUID = -1;
	private static final int version = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);

		SerializationHelper.writeSafeUTF(out, script);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		int ver = in.readInt();

		// Switch on the version of the class so that version changes can be
		// elegantly handled.
		if (ver == 1)
			script = SerializationHelper.readSafeUTF(in);
	}
}
