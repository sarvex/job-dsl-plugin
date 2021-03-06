package javaposse.jobdsl.dsl.jobs

import javaposse.jobdsl.dsl.ContextHelper
import javaposse.jobdsl.dsl.DslContext
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.WithXmlAction
import javaposse.jobdsl.dsl.helpers.AxisContext

class MatrixJob extends Job {
    MatrixJob(JobManagement jobManagement) {
        super(jobManagement)
    }

    void axes(@DslContext(AxisContext) Closure closure) {
        AxisContext context = new AxisContext()
        ContextHelper.executeInContext(closure, context)

        withXmlActions << WithXmlAction.create { Node project ->
            Node axesNode = project / 'axes'
            context.axisNodes.each {
                axesNode  << it
            }
            context.configureBlocks.each {
                new WithXmlAction(it).execute(axesNode)
            }
        }
    }

    /**
     * <combinationFilter>axis_label=='a'||axis_label=='b'</combinationFilter>
     */
    void combinationFilter(String filterExpression) {
        withXmlActions << WithXmlAction.create { Node project ->
            Node node = methodMissing('combinationFilter', filterExpression)
            project / node
        }
    }

    /**
     * <executionStrategy>
     *     <runSequentially>false</runSequentially>
     * </executionStrategy>
     */
    void runSequentially(boolean sequentially = true) {
        withXmlActions << WithXmlAction.create { Node project ->
            Node node = methodMissing('runSequentially', sequentially)
            project / 'executionStrategy' / node
        }
    }

    /**
     * <executionStrategy>
     *     <touchStoneCombinationFilter>axis_label=='a'||axis_label=='b'</touchStoneCombinationFilter>
     *     <touchStoneResultCondition>
     *         <name>UNSTABLE</name>
     *         <ordinal>1</ordinal>
     *         <color>YELLOW</color>
     *         <completeBuild>true</completeBuild>
     *     </touchStoneResultCondition>
     * </executionStrategy>
     */
    void touchStoneFilter(String filter, boolean continueOnUnstable = false) {
        withXmlActions << WithXmlAction.create { Node project ->
            project / 'executionStrategy' / 'touchStoneCombinationFilter'(filter)
            project / 'executionStrategy' / 'touchStoneResultCondition' {
                name continueOnUnstable ? 'UNSTABLE' : 'STABLE'
                color continueOnUnstable ? 'YELLOW' : 'BLUE'
                ordinal continueOnUnstable ? 1 : 0
            }
        }
    }
}
